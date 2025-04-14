<!-- filepath: camera-example-app/src/App.svelte -->
<script>
  import Camera from './lib/Camera.svelte';

  let pictureUrl = '';

  export function handlePictureTaken(url) {
    pictureUrl = url;
    window.alert('Picture taken! URL: ' + url);
  }
  export function handleVideoRecorded(url) {
    window.alert('Video recorded! URL: ' + url);
  }
</script>

<main class="container">
  <h1>Camera Example App</h1>
  <Camera on:pictureTaken={handlePictureTaken} on:videoRecorded={handleVideoRecorded} />
  {#if pictureUrl}
    <h2>Picture Taken:</h2>
    <img src={pictureUrl} alt="Captured Image" />
  {/if}
</main>

<style>
  /* Add any additional styles here */
</style>