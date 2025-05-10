use tauri::AppHandle;
use tauri_plugin_camera::{CameraExt, TakePictureResponse};

#[tauri::command]
fn take_picture(app: AppHandle) -> TakePictureResponse {
    let camera = app.camera();
    
    // Call the camera plugin to take a picture
    let response = camera.take_picture().unwrap();
    dbg!("Response: {:?}", &response);
    response
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![take_picture])
        .plugin(tauri_plugin_camera::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
