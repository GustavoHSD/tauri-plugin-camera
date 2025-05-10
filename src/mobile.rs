use tauri::{ plugin::PluginHandle, Runtime };

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_camera);

#[derive(Debug)]
/// Access to the camera APIs.
pub struct Camera<R: Runtime>(pub PluginHandle<R>);

impl<R: Runtime> Camera<R> {
    pub fn inner(&self) -> &PluginHandle<R> {
        &self.0
    }
}

impl<R: Runtime> Camera<R> {
    pub fn take_picture(&self) -> crate::Result<TakePictureResponse> {
        self.0.run_mobile_plugin("takePicture", ()).map_err(Into::into)
    }

    pub fn record_video(&self) -> crate::Result<RecordVideoResponse> {
        self.0.run_mobile_plugin("recordVideo", ()).map_err(Into::into)
    }
}
