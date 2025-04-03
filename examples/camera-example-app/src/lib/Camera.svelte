<script>
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

  async function takePicture() {
    if (!isCameraInitialized) {
      alert('Please initialize the camera first.');
      return;
    }

    try {
      // Call the plugin API to take a picture
      console.log('Taking picture...');
    } catch (error) {
      console.error('Failed to take picture:', error);
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
