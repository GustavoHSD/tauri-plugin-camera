// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\models.rs
use serde::{ Deserialize, Serialize };

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TakePictureResponse {
    pub image_data: String,
    pub width: u32,
    pub height: u32,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordVideoResponse {
    pub video_data: String,
    pub width: u32,
    pub height: u32,
}
