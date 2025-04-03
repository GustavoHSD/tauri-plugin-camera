// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\src\models.rs
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct TakePictureRequest {
    pub quality: u32,
}

#[derive(Serialize, Deserialize)]
pub struct TakePictureResponse {
    pub file_path: String,
}

#[derive(Serialize, Deserialize)]
pub struct RecordVideoRequest {
    pub duration: u32,
}

#[derive(Serialize, Deserialize)]
pub struct RecordVideoResponse {
    pub file_path: String,
}