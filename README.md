# Virtual-Try-On-Android-App-Client-Side
This project was finihsed in May 2020. 

*The author does not have the server side code.*

*We do not have money to keep the server running so now it cannot work.*


Functions:

a. When the user enters the system, the system displays available target clothes.

b. After the user clicks on an image, the system jumps to the photo upload page. 

c. After the user clicks the button, the system accesses the album on the mobile phone. 

d. The system enters the cropping page after the user selects an image, so that the user can drag a cropping box to crop. The cropping box is of a fixed (width/length).

e. The system sends the image to the server for validation checking after cropping. It receives the feedback from the server and warn the user for the unqualified photos. The server deals with the qualified photos and returns the final effect of virtual try-on to the user.


Interactions with server:

a. receiving all clothing file paths.

b. sending a clothing file path and receiving the image of clothing (byte stream). 

c. sending an image of person (byte stream) and receiving the image path of the person.

d. sending an image path of a clothing and an image path of a person and receiving the image path of the final virtual try-on result.

e. sending the try-on image path and receiving the image of the try-on result (byte stream).


Tips:

a. Version issue. 

The suitable version of Android is Android 8.0.

b. Permission issue. 

When accessing (especially modifying) local files of the mobile phone, it would be better to pay special attention to whether all the required permissions are turned on. 
For example, network permissions include network permissions (android.permission.INTERNET), obtaining network status permissions (android.permission.ACCESS_NETWORK_STATE), changing network state (android.permission.CHANGE_NETWORK_STATE), changing WIFI state (android.permission.CHANGE_WIFI_STATE), and so on. 
Storage permissions include reading external storage permissions (android.permission.READ_EXTERNAL_STORAGE), writing external storage permissions (android.permission.WRITE_EXTERNAL_STORAGE), and so on.
When there is a need to turn on the camara or cropping local images, you also need to provide File Provider Paths. 
When the local media files are involved, you may need to add the corresponding URI permissions (FLAG_GRANT_READ_URI_PERMISSION, FLAG_GRANT_WRITE_URI_PERMISSION). 
The image cropping based on the com.android.camera.action.CROP. Cropping would fail without the corresponding URI permission.

c. Local media type issue.

After the user picks up am image from the local album of the mobile phone, the system will first consider the Android version. 
The Storage access Framework (SAF) has been introduced from Android 4.4. The framework provides convenience for users to browse the local files from Apps. 
Each type of file has a corresponding documents provider. 
From Android 4.4, the system will determine whether the file is an external storage file (isExternalStorageDocument method), whether it is a download file (isDownloadsDocument method), whether it is a media file (isMediaDocument method). The system will classify and parse the file URI, determine the file type from the URI, and generate a file path (String). 
After Android version 7.0, the security restriction keeps enhancing, and the modification on local media files of the mobile phone must be processed through a shared folder. 
In order to crop the images through the shared folder, the system will generate a new URI through the FileProvider.getUriForFile method.

d. Request head issue.

In many cases, the use of a general content type (Content-Type:application/octet-stream) may bring convenience to complex applications and enhance the reusability of the code. However, it is only applicable to the case where the request head is not used for any decision on the server side.
For instance, the server may assume the content type in the request head is a specific image type and remove 'image/' from the content type to be the suffix of the filename (such as remove 'image/' from 'image/jpg' and let 'jpg' by the suffix). It may not cause an error immediately when you add an invalid filename suffix, while the file with an invalid filename in the server will return an internal error 500 when you try to access it next time.

e. Image upload issue.

Before the image is uploaded to the server, a cache file should be generated, which is stored in the cache folder established by the system with the time(second) as the file name. 
After successfully generating the cache file, set up the file upload listener and add the file upload thread to the processing queue.
