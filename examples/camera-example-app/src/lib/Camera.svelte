<script>
  import { invoke } from '@tauri-apps/api/core';
  import { emit } from '@tauri-apps/api/event';
  
  export async function takePicture() {
    console.log("Taking picture...");
    const response = await invoke('plugin:camera|take_picture');
    console.log("Picture taken from Câmera:", response);
    emit('pictureTaken', { url: response });
    return response;
  }

  export async function recordVideo() {
    console.log("Taking Video...");
    const response = await invoke('plugin:camera|record_video');
    console.log("Video taken from Câmera:", response);
    emit('videoRecorded', { url: response });
    return response;
  }

  let isCameraInitialized = false;

</script>

<div>
  <button on:click={takePicture}>Take Picture</button>
  <button on:click={recordVideo}>Record Video</button>
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
