const COMMANDS: &[&str] = &["take_picture", "record_video"];

fn main() {
  tauri_plugin::Builder::new(COMMANDS)
    .android_path("android")
    .ios_path("ios")
    .build();
}