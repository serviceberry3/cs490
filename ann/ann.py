import cv2
from PySide6.QtWidgets import (
    QApplication,
    QFrame,
    QListWidget,
    QListWidgetItem,
    QPushButton,
    QMainWindow,
    QGridLayout,
    QLineEdit,
    QLabel,
    QErrorMessage,
    QComboBox
)
from dialogs import FixedWidthMessageDialog, FW_FONT
import sys
import os

VIDS_DIR = "/home/nodog/docs/files/YaleSenior/cs490/cs490/data_prep/vids"

def absoluteFilePaths(directory):
    for dirpath, _, filenames in os.walk(directory):
        for f in filenames:
            #yield suspends function’s execution and sends value back to caller, but retains state to enable fxn to resume where left off. When the function resumes, it continues execution immediately after the last yield run. 
            #This allows it to produce series of values over time, rather than computing them at once and sending them back like a list.
            yield os.path.abspath(os.path.join(dirpath, f))


class AnnotatorGUI:
  def __init__(self):
    """Overhead management of GUI"""
    self.vid = None

    self.app = QApplication()

    # Highest level layout: input & output
    page_layout = QGridLayout()

    # Manager for user unput interaction: entries and submit
    user_layout = QGridLayout()

    # Manager for entries: label & line (included in user_layout)
    entry_layout = QGridLayout()


    #Delegate function for simple reg searching
    def search_call_back():
      return None

    # Add four fields to the application window for searching
    dept_layout = AnnotatorGUI._create_entry('Search for product:')
    entry_layout.addLayout(dept_layout, 0, 0)

    # QPushButton that helps reg.py communicate with regserver.py
    vid_button = QPushButton('Play Video')
    vid_button.clicked.connect(self.play_vid)

    search_button = QPushButton('Search')
    search_button.clicked.connect(search_call_back)

    vids_dropdown = QComboBox()
    vids_dropdown.addItems(absoluteFilePaths(VIDS_DIR))
    vids_dropdown.currentTextChanged.connect(self.vid_text_changed)

    user_layout.addWidget(vids_dropdown, 1, 0)

    # Merging top level user input layouts
    user_layout.addLayout(entry_layout, 0, 0)
    user_layout.addWidget(vid_button, 2, 0)
    user_layout.addWidget(search_button, 1, 1)

    # Output layout, list automatically horizontally/vertically scrolls
    self.output_widget = QListWidget()
    self.output_widget.setFont(FW_FONT)

    # Event: double clicked or enter (or Cmd+O for Mac)
    #self.output_widget.itemActivated.connect(details_call_back)

    page_layout.addLayout(user_layout, 0, 0)
    page_layout.addWidget(self.output_widget, 1, 0)

    self.frame = QFrame()
    self.frame.setLayout(page_layout)

    self._create_window()



  def vid_text_changed(self, text):
    self.vid = text


  #A static method doesn't receive any reference argument whether it is called by an instance of a class or by the class itself
  @staticmethod
  def _create_entry(name:str) -> QGridLayout:
      """Creates a label and lineedit for {name} with callbacks
      update_cb: method for storing the line edit's text
      query_cb: method for submitting the query request
      """

      label = QLabel(name)
      entry = QLineEdit()
      
      '''
      entry.textChanged.connect(
          lambda text: update_cb(text)
      )
      entry.returnPressed.connect(query_cb)'''
      layout = QGridLayout()
      layout.addWidget(label, 0, 0)
      layout.addWidget(entry, 0, 1)

      return layout


  def _create_window(self):
        """Develops the default window"""

        # Create window and add frame
        self.window = QMainWindow()
        self.window.setWindowTitle('Annotation System')
        self.window.setCentralWidget(self.frame)

        # Ensures dialog size will be <= 25% of window
        screen_size = self.app.primaryScreen().availableGeometry()
        self.window.resize(screen_size.width() // 2, screen_size.height() // 2)

  def run(self):
        """Creates GUI and responses to events"""

        self.window.show()
        sys.exit(self.app.exec())


  def play_vid(self):
    #in inches
    aisle_dist = 965
    aisle_width = 107

    #open the video using VideoCapture obj
    cap = cv2.VideoCapture(self.vid)

    if (cap.isOpened() == False): 
      print("ERROR opening video stream or file")
      exit()

    #get video fps
    fps = cap.get(cv2.CAP_PROP_FPS)
    print(f"{fps} frames per second")

    #get num of frames
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

    if (fps == 0.0):
      print("ERROR: get returned 0.0 frames per second")
      exit()

    print("frame_count is", frame_count)
    duration = frame_count / fps
    print("Duration of video is", duration, "seconds")

    framectr = 0
    
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
            if key == ord('p') or key == ord(' '):
                print("Paused at frame number", framectr, "and at time", framectr/fps)
                cv2.waitKey(-1) #wait until any key is pressed

            framectr += 1

        #something wrong opening frame, so break out of loop now
        else: 
            break
    

    #release video capture obj
    cap.release()
    
    #close all cv2 windows
    cv2.destroyAllWindows()


if __name__ == "__main__":
    gui = AnnotatorGUI()
    gui.run()