<script>
  import { invoke } from '@tauri-apps/api/core';
  
  export async function takePicture() {
    console.log("Taking picture...");
    const response = await invoke('plugin:camera|take_picture');
    console.log("Picture taken:", response);
    return response;
  }

  export async function recordVideo() {
    const response = await invoke('plugin:camera|record_video');
    return response;
  }

  let isCameraInitialized = false;

  async function initializeCamera() {
    try {
      // Call the plugin API to initialize the camera
      console.log('Initializing camera...');
      isCameraInitialized = true;
    } catch (error) {
      console.error('Failed to initialize camera:', error);
    }
  }
</script>

<div>
  <button on:click={initializeCamera}>Initialize Camera</button>
  <button on:click={takePicture} disabled={!isCameraInitialized}>Take Picture</button>
</div>

<style>
  div {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  button {
    padding: 0.5rem 1rem;
    font-size: 1rem;
    cursor: pointer;
  }

  button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
</style>
