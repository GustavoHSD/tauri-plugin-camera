// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\mobile.rs
use serde::de::DeserializeOwned;
use tauri::{
  plugin::{PluginApi, PluginHandle},
  AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_camera);


// initializes the Kotlin plugin classes
// pub fn init<R: Runtime, C: DeserializeOwned>(
//   _app: &AppHandle<R>,
//   api: PluginApi<R, C>,
// ) -> crate::Result<Camera<R>> {
//   #[cfg(target_os = "android")]
//   let handle = api.register_android_plugin("app.tauri.camera", "CameraPlugin")?;
//   Ok(Camera(handle))
// }

/// Access to the camera APIs.
pub struct Camera<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> Camera<R> {
  pub fn inner(&self) -> &PluginHandle<R> {
    &self.0
  }
  
  pub fn new(handle: PluginHandle<R>) -> Self {
    Camera(handle)
}
}

impl<R: Runtime> Camera<R> {
  pub fn take_picture(&self, payload: TakePictureRequest) -> crate::Result<TakePictureResponse> {
    self
      .0
      .run_mobile_plugin("take_picture", payload)
      .map_err(Into::into)
  }

  pub fn record_video(&self, payload: RecordVideoRequest) -> crate::Result<RecordVideoResponse> {
    self
      .0
      .run_mobile_plugin("record_video", payload)
      .map_err(Into::into)
  }
}