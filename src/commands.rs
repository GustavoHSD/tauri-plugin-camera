// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\commands.rs
use serde::{Deserialize, Serialize};
use tauri::command;

use crate::mobile::Camera;

use crate::models::{TakePictureRequest, TakePictureResponse, RecordVideoRequest, RecordVideoResponse};
use crate::CameraExt;

#[derive(Deserialize)]
pub struct TakePictureArgs {
    pub request: TakePictureRequest,
}

#[derive(Serialize)]
pub struct TakePictureResult {
    pub response: TakePictureResponse,
}

// #[command]
// pub async fn take_picture(app_handle: tauri::AppHandle, args: TakePictureArgs) -> Result<TakePictureResult, String> {
//     //let camera = state.camera();
//     let camera = app_handle.camera();
//     let response = camera.take_picture(args.request).map_err(|e| e.to_string())?;
//     Ok(TakePictureResult { response })
// }

#[derive(Deserialize)]
pub struct RecordVideoArgs {
    pub request: RecordVideoRequest,
}

#[derive(Serialize)]
pub struct RecordVideoResult {
    pub response: RecordVideoResponse,
}

#[command]
pub async fn record_video(app_handle: tauri::AppHandle,  args: RecordVideoArgs) -> Result<RecordVideoResult, String> {
    //let camera = state.camera();
    let camera = app_handle.camera();
    let response = camera.record_video(args.request).map_err(|e| e.to_string())?;
    Ok(RecordVideoResult { response })
}