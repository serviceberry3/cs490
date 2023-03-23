import cv2


#open the video using VideoCapture obj
cap = cv2.VideoCapture('../vids/aisle_left.mp4')

if (cap.isOpened() == False): 
  print("Error opening video stream or file")
 
#read frames until video is done
while(cap.isOpened()):
    #read a frame
    ret, frame = cap.read()

    #make sure frame read successfully
    if ret == True:
        #create the cv2 window and give it name
        cv2.namedWindow("aisle", cv2.WINDOW_NORMAL)

        #resize window to custom size
        cv2.resizeWindow("aisle", 500, 900)

        #display a frame in the window
        cv2.imshow("aisle", frame)

        #can press q to quit
        #if cv2.waitKey(25) & 0xFF == ord('q'):
        #    break

        key = cv2.waitKey(1)
        if key == ord('q'):
            break
        if key == ord('p'):
            cv2.waitKey(-1) #wait until any key is pressed

    #something wrong opening frame, so break out of loop now
    else: 
        break
 

#release video capture obj
cap.release()
 
#close all cv2 windows
cv2.destroyAllWindows()