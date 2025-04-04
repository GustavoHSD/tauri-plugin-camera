// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\models.rs
use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TakePictureRequest {
    pub quality: u32,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct TakePictureResponse {
    pub file_path: String,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordVideoRequest {
    pub duration: u32,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordVideoResponse {
    pub file_path: String,
}