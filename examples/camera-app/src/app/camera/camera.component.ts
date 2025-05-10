import { Component, signal } from "@angular/core";
import { invoke } from "@tauri-apps/api/core";
import { emit } from "@tauri-apps/api/event";


export interface TakePictureResponse {
  imageData: string;
    width: number;
    height: number;
}
export interface TakeVideoResponse {
  videoData: string;
    width: number;
    height: number;
}


@Component({
    selector: "app-camera",
    imports: [],
    templateUrl: "./camera.component.html",
    styleUrl: "./camera.component.scss",
})
export class CameraComponent {
    takenVideo = signal<TakeVideoResponse | null>(null);
    takenPicture = signal<TakePictureResponse | null>(null);
    emptyReturn = false;

    async recordVideo() {
        console.log("Taking Video...");
        try {
            this.takenVideo.set(null);
            const response = await invoke("plugin:camera|record_video");
            console.log("Video taken from Câmera:", response);
            emit("videoRecorded", { url: response });
            this.takenVideo.set(response as TakeVideoResponse);
        } catch (error) {
            console.error("Error recording video:", error);
            this.emptyReturn = true;
        }
    }

    async takePicture() {
        console.log("Taking picture...");
        try {
            this.takenPicture.set(null);
            const response = await invoke("plugin:camera|take_picture");
            console.log("Picture taken from Câmera:", response);
            emit("pictureTaken", { url: response });
            this.takenPicture.set(response as TakePictureResponse);
        } catch (error) {
            console.error("Error taking picture:", error);
            this.emptyReturn = true;
        }
    }
}
