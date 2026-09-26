# Filament 3D Engine: Lifecycle Research, Version Caveats & Crash Analysis

> **IMPORTANT WARNING FOR FUTURE MAINTAINERS:**  
> **DO NOT** blindly bump `filamentAndroid` in `gradle/libs.versions.toml` without reading this document. Upgrading Filament beyond `1.54.5` without decoupling or vendoring `ModelViewer.kt` will immediately reintroduce a fatal native crash (`SIGSEGV` in `Camera_nLookAt`) that crashes the entire Linux process and cannot be caught by Kotlin `try/catch` blocks.

### Quick Search Index & Diagnostic Keywords
For developers and maintainers grepping error logs or searching the codebase:
* **Crash Signatures:** `Java_com_google_android_filament_Camera_nLookAt`, `Camera_nLookAt`, `nLookAt`, `Camera.lookAt`, `FCamera::lookAt`, `fault addr 0x1268`, `SIGSEGV (code 1, SEGV_MAPERR)`, `Fatal signal 11`, `FTransformManager::setTransform`, `FEngine::getTransformManager`, `libfilament-jni.so`, `libfilament-utils-jni.so`.
* **Classes & Methods:** `ModelViewer.kt`, `onViewDetachedFromWindow`, `ModelViewer.destroy()`, `ModelViewer.render()`, `ModelViewer.doRender()`, `FrameCallback.doFrame`, `UiHelper`, `UiHelper.isReadyToRender`, `UiHelper.attachTo`, `DisplayHelper`.
* **Libraries & Artifacts:** `com.google.android.filament:filament-android`, `com.google.android.filament:filament-utils-android`, `com.google.android.filament:gltfio-android`, `filamat-android`.
* **Subsystems & Issues:** `defaultCameraManipulator(0, 0)`, `FramePacer`, `DISPLAY_STUFFED`, `DISPLAY_STARVING`, `CameraManipulator`, `BLASTBufferQueue`, `SurfaceView` in Jetpack Compose, `AndroidView` detach lifecycle.

---

## 1. Executive Summary: The Fatal Crash

When upgrading Filament from `1.54.5` (used in working commit [`641b21a`](https://github.com/shaikhmohammadtalha/android-anatomy-insight/commit/641b21a1ef0c59ce071e97f6f0676d915663b557)) to `1.77.1`+ (introduced in `b5a12a8`), the app immediately crashes upon loading any 3D model with the following native tombstone:

```text
Fatal signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x1268 in tid 28155 (.anatomyinsight), pid 28155 (.anatomyinsight)
Cmdline: com.shaikhmohammadtalha.anatomyinsight
pid: 28155, tid: 28155, name: .anatomyinsight  >>> com.shaikhmohammadtalha.anatomyinsight <<<
      #00 pc 000000000018eebc  /data/app/.../libfilament-jni.so (FTransformManager::setTransform)
      #01 pc 0000000000130d44  /data/app/.../libfilament-jni.so (Java_com_google_android_filament_Camera_nLookAt+52)
      #25 pc 00000000000017a8  [anon:dalvik-classes6.dex] (com.shaikhmohammadtalha.anatomyinsight.rendering.ModelRenderer$FrameCallback.doFrame+0)
```

Because this is a native Linux segmentation fault (`SIGSEGV`), the Android ART runtime terminates the process instantly without throwing a standard Java `Exception`.

---

## 2. Root Cause Breakdown: `1.54.5` vs `1.77.1+`

### 2.1 The Breaking Change in `ModelViewer.kt`
`ModelViewer.kt` is part of the `filament-utils-android` library (`com.google.android.filament:filament-utils-android`). In newer releases, Google Filament engineers introduced an aggressive detach listener in the secondary constructor:

#### Filament 1.54.5 (Working):
```kotlin
private fun addDetachListener(view: android.view.View) {
    view.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: android.view.View) {}
        override fun onViewDetachedFromWindow(v: android.view.View) {
            uiHelper.detach() // Safe: releases the native Surface/SwapChain only
        }
    })
}
```
* **Behavior:** When the view detaches, only `uiHelper.detach()` is called. The underlying Filament `Engine`, `Camera`, `Scene`, and `View` remain intact. When `doFrame()` runs, `uiHelper.isReadyToRender` returns `false`, and `render()` safely skips rendering.

#### Filament 1.77.1+ (Crashing):
```kotlin
private fun addDetachListener(view: android.view.View) {
    view.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: android.view.View) {}
        override fun onViewDetachedFromWindow(v: android.view.View) {
            destroy() // FATAL: Calls engine.destroyCameraComponent and engine.destroy()!
        }
    })
}
```
* **Behavior:** When `onViewDetachedFromWindow()` fires, it calls `destroy()`:
  ```kotlin
  fun destroy() {
      uiHelper.detach()
      destroyModel()
      ...
      engine.destroyCameraComponent(camera.entity)
      EntityManager.get().destroy(camera.entity)
      engine.destroy()
  }
  ```

---

## 3. Why Jetpack Compose Triggers This Crash

In Jetpack Compose:
1. `AndroidView` hosts the `SurfaceView`.
2. When the user selects a model, Compose recomposes `MainActivityContent` (e.g. updating `currentModel`, updating `showMainModels`, or recalculating window insets/system bar padding).
3. During layout passes or when switching views (navigating to Search, Settings, History, or toggling between models), Compose temporarily detaches or remeasures the `SurfaceView`.
4. In Filament 1.77.1+, `onViewDetachedFromWindow()` executes immediately and permanently destroys the native `Engine` and `CameraComponent`.
5. However, `ModelRenderer` in Kotlin and Android's `Choreographer` are unaware that the C++ object behind `modelViewer` was destroyed.
6. When the `Choreographer` frame ticks:
   ```kotlin
   override fun doFrame(frameTimeNanos: Long) {
       modelViewer.render(frameTimeNanos)
   }
   ```
7. Inside `modelViewer.render()` -> `doRender()`:
   ```kotlin
   cameraManipulator?.let { cm ->
       cm.getLookAt(eyePos, target, upward)
       camera.lookAt(...) // Invokes Java_com_google_android_filament_Camera_nLookAt
   }
   ```
8. The JNI call passes `mNativeObject` of the already-destroyed `Camera`.

---

## 4. Deep C++ Engine Internals: Why Offset `0x1268`?

In Filament's native C++ source (`filament/src/details/Camera.cpp`):
```cpp
void FCamera::lookAt(double3 const& eye, double3 const& center, double3 const& up) noexcept {
    FTransformManager& transformManager = mEngine.getTransformManager();
    transformManager.setTransform(transformManager.getInstance(mEntity),
            mat4::lookAt(eye, center, up));
}
```

In `FEngine`:
* `mTransformManager` is stored at struct offset `0x1268`.
* When `FCamera::lookAt` runs on a camera whose `mEngine` reference points to freed memory (or `this` is invalid):
  ```arm64
  ldr x0, [engine, #0x1268]   // Tries to read transformManager from NULL (0x0 + 0x1268)
  ```
* Dereferencing `0x0 + 0x1268` triggers `SEGV_MAPERR fault addr 0x1268`.

---

## 5. Additional Issues in Filament 1.77.1+

1. **`defaultCameraManipulator(0, 0)` Aspect Ratio NaN:**
   In 1.77.1+, `ModelViewer` constructs `defaultCameraManipulator(surfaceView.width, surfaceView.height)`. When `SurfaceView` is instantiated in `AndroidView(factory = ...)`, its dimensions are initially `0x0`. Constructing a `Manipulator` with `viewport(0, 0)` can cause internal matrix divisions by zero (`0/0 = NaN`), filling `eyePos`, `target`, and `upward` vectors with `NaN` before the surface layout pass completes.
2. **`FramePacer` Display Stuffed / Frame Drop Telemetry:**
   Filament 1.77.1+ introduced a `FramePacer` subsystem that calculates pacing status (`DISPLAY_STUFFED`, `DISPLAY_STARVING`). If the main thread experiences GC or asset loading stalls (e.g. loading a 15MB GLB model into a byte buffer), `FramePacer` enters degenerate pacing states, dropping presentation frames and hitching.

---

## 6. Safe Architecture & Rules for Future Upgrades

If a future maintainer needs to upgrade Filament beyond `1.54.5`:

### Rule 1: NEVER use the default `ModelViewer(surfaceView, ...)` constructor from `filament-utils-android`
The secondary constructor attaches the fatal `addDetachListener(view) { destroy() }`.
To upgrade safely, you must either:
- **Option A (Recommended):** Vendor `ModelViewer.kt` directly into `app/src/main/java/com/shaikhmohammadtalha/anatomyinsight/rendering/` and strip out `addDetachListener(surfaceView)`.
- **Option B:** Use the primary constructor `ModelViewer(engine, uiHelper)` and manage `surfaceView` attachments manually via `uiHelper.attachTo(surfaceView)` without any `OnAttachStateChangeListener` auto-destructor.

### Rule 2: Multi-Guard the Frame Render Loop
`ModelRenderer.kt` must always guard the render loop against uninitialized or zero-sized surfaces:
```kotlin
private inner class FrameCallback : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
        if (surfaceInitialized && 
            isModelLoaded && 
            this@ModelRenderer::surfaceView.isInitialized && 
            surfaceView.width > 0 && 
            surfaceView.height > 0 && 
            this@ModelRenderer::uiHelper.isInitialized && 
            uiHelper.isReadyToRender) {
            try {
                modelViewer.render(frameTimeNanos)
            } catch (e: Exception) {
                Log.e(TAG, "Render frame error: ${e.message}", e)
            }
        }
        // ONLY reschedule if the surface is actively initialized!
        if (surfaceInitialized && this@ModelRenderer::choreographer.isInitialized) {
            choreographer.postFrameCallback(this)
        }
    }
}
```

### Rule 3: Stop Choreographer Loops on View Disposal
Do not allow `Choreographer.FrameCallback` to run indefinitely when the user leaves the 3D screen. Connect disposal to Jetpack Compose `DisposableEffect`:
```kotlin
DisposableEffect(Unit) {
    onDispose {
        renderer.destroy()
    }
}
```

### Rule 4: SurfaceView Hierarchy in Compose
- Avoid burying `SurfaceView` beneath inset-shifting layout containers (like `TopAppBar` with dynamic padding inside a `Scaffold`).
- Keep the `SurfaceView` container (`Box`) starting at `y = 0` at the top of the main window to prevent BLAST queue relayout anomalies and tearing.
- Any model or subpart titles should be displayed as overlay badges (e.g. `Surface(shape = RoundedCornerShape(16.dp))` aligned to `Alignment.TopCenter`) rather than shifting the 3D view container down with status bar insets.

---

## 7. Current Stable Configuration

| Property | Value | Notes |
| :--- | :--- | :--- |
| `filamentAndroid` | `1.54.5` | Stable, matches commit `641b21a` |
| `gltfio-android` | `1.54.5` | Matches core Filament version |
| `filament-utils-android` | `1.54.5` | Safe detach behavior (`uiHelper.detach()`) |
| `Room Database` | Version `8` | Contains `models`, `subparts`, `notes`, `history` |
| Asset DB | `anatomy_database_fixed.db` | Pre-packaged with clean empty `history` and `notes` tables |
