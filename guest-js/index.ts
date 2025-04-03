// filepath: e:\Users\charles\Documents\Projects\Typescript\tauri-plugin-camera\guest-js\index.ts
import { invoke } from '@tauri-apps/api/core';

export async function takePicture(): Promise<string> {
  const response = await invoke('take_picture');
  return response as string;
}

export async function recordVideo(): Promise<string> {
  const response = await invoke('record_video');
  return response as string;
}