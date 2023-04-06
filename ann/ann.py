import cv2
from PySide6 import QtCore, QtGui, QtWidgets

from PySide6.QtWidgets import *
from PySide6.QtCore import *
from PySide6.QtGui import *

from dialogs import FixedWidthMessageDialog, FW_FONT
import sys
import os
from communication import QueryArgs, QueryType, Query
from lookup import DataLookup
from table import Table

from constants import *
from loc import *



VIDS_DIR = "/home/nodog/docs/files/YaleSenior/cs490/cs490/data_prep/vids"

def absoluteFilePaths(directory):
    for dirpath, _, filenames in os.walk(directory):
        for f in filenames:
            #yield suspends function’s execution and sends value back to caller, but retains state to enable fxn to resume where left off. When the function resumes, it continues execution immediately after the last yield run. 
            #This allows it to produce series of values over time, rather than computing them at once and sending them back like a list.
            yield os.path.abspath(os.path.join(dirpath, f))


class KeyPressFilter(QObject):
  def eventFilter(self, widget, event):
    if event.type() == QEvent.KeyPress:
      text = event.text()
      
      if event.modifiers():
          text = event.keyCombination().key().name.decode(encoding="utf-8")

      if text == 'q':
        exit()

    return False


#layout setup (large to small): frame -> page_layout -> user_layout -> [(textedit_bars_layout -> product name search bar, subcat searchbar), vid dropdown, mode dropdown, etc.]

class AnnotatorGUI(QWidget):
  def __init__(self, app, parent=None):
    super().__init__(parent=parent)

    """Overhead management of GUI"""
    self.vids = absoluteFilePaths(VIDS_DIR)

    #print(cv2.EVENT_LBUTTONDBLCLK)

    self.vid = next(self.vids)
    self.vid_shortname = None
    #print("init: self.vid is", self.vid)

    #mode of annotation -- are we annotating to label location of specific products or just the subcategories?
    self.mode = None
    self.user_args = QueryArgs(None, "", "")

    #the GUI's underlying Qt application instance
    self.app = app

    self.selected_prod = None

    #self.eventFilter = KeyPressFilter(parent=self)
    #self.installEventFilter(self.eventFilter)

    # Highest level layout: input & output
    page_layout = QGridLayout()

    # Manager for user unput interaction: entries and submit
    user_layout = QGridLayout()

    # Manager for entries: label & line (included in user_layout)
    textedit_bars_layout = QGridLayout()

    #callback fxn for if item is double clicked
    def prod_seln_call_back(item: QListWidgetItem):
      #get prodid from item string
      text = item.data(0)
      #print("text is", text)
      fields = text.split(' ')

      prod_id = int(fields[0])

      #now use the product ID to fetch the other fields from the db
      #construct the query
      my_query = Query(QueryArgs(prod_id, None, None))
      res = DataLookup(my_query.query_args, True).data #should only have one result

      print("res is", res)

      first_res = res[0]

      name = first_res[1]
      aisle = first_res[2]
      rootCatId = first_res[3]
      rootCatName = first_res[4]
      subCatId = first_res[5]
      subCatName = first_res[6]

      #print(f"You've selected product with id {prod_id}, name {name}, aisle {aisle}, rootCatId {rootCatId}, rootCatName {rootCatName}, subCatId {subCatId}, and subCatName {subCatName}")

      #instantiate a product from selected one
      self.selected_prod = Product(prod_id, name, aisle, rootCatId, rootCatName, subCatId, subCatName)

      #when item is double clicked or enter is hit, highlight item in given color indicating that it's selected for the annotation
      item.setBackground(PROD_SELECT_COLOR)

      self.output_widget.clearSelection()

      self.setWindowTitle(CLICK_IN_VIDEO_HELP_TXT)
      cv2.setWindowProperty(self.vid_shortname, cv2.WND_PROP_TOPMOST, 1)

      #detail_popup = FixedWidthMessageDialog(f"Details for class {crn}", client.data)
      #detail_popup.exec()

    #function called on hitting enter OR pressing search button. query the db.
    def search_call_back():
      self.app.setOverrideCursor(QCursor(Qt.WaitCursor))

      #construct the query
      query = Query(self.user_args)

      res = DataLookup(query.query_args)

      #print(res.data)

      #calling res.data will trigger the DataLookup instance's getter fxn
      out_table = Table(["", "", "", "", "", "", ""], res.data, format_str = "ppppppp", col_sep=" ", max_width=float('inf'))
      out_val = [row[0] for row in out_table]

      #populate results list
      self.output_widget.clear()

      for entry in out_val:
          item = QListWidgetItem(entry)
          self.output_widget.addItem(item)

    #add fields to application window for searching for prods
    prodname_layout = AnnotatorGUI._create_entry('Product name:', self.user_args.set_prodname, search_call_back)
    textedit_bars_layout.addLayout(prodname_layout, 0, 0)

    subcat_layout = AnnotatorGUI._create_entry('Product\'s subcategory:', self.user_args.set_subcat, search_call_back)
    textedit_bars_layout.addLayout(subcat_layout, 0, 1)

    # QPushButton that plays the video
    vid_button = QPushButton('Play Video/Begin Annotation Workflow')
    vid_button.clicked.connect(self.play_vid)

    search_button = QPushButton('Search')
    search_button.clicked.connect(search_call_back)

    textedit_bars_layout.addWidget(search_button, 0, 2)

    #create gridlayout for the video dropdown area
    vids_section = QGridLayout()

    vid_dropdown_txt = QLabel("Select video:")
    #dropdown menu to select which video to play
    vids_dropdown = QComboBox()
    vids_dropdown.addItems(absoluteFilePaths(VIDS_DIR))
    vids_dropdown.currentTextChanged.connect(self.vid_text_changed)

    vids_section.addWidget(vid_dropdown_txt, 0, 0)
    vids_section.addWidget(vids_dropdown, 0, 1)

    #create gridlayout for the mode dropdown area
    mode_section = QGridLayout()

    mode_dropdown_txt = QLabel("I am labeling:")
    #dropdown menu to select whether to annotate using subcategory or specific product
    mode_dropdown = QComboBox()
    mode_dropdown.addItems(["subcategories (i.e. Sandwich Cookies)", "specific products"])
    mode_dropdown.currentTextChanged.connect(self.mode_text_changed)

    mode_section.addWidget(mode_dropdown_txt, 0, 0)
    mode_section.addWidget(mode_dropdown, 0, 1)

    user_layout.addLayout(textedit_bars_layout, 0, 0)
    user_layout.addLayout(vids_section, 1, 0)
    user_layout.addLayout(mode_section, 2, 0)

    #merging top level user input layouts
    user_layout.addWidget(vid_button, 3, 0)
    

    #output layout for the results list, list automatically horizontally/vertically scrolls
    self.output_widget = QListWidget()
    self.output_widget.setFont(FW_FONT)

    # Event: double clicked or enter (or Cmd+O for Mac)
    self.output_widget.itemActivated.connect(prod_seln_call_back)

    page_layout.addLayout(user_layout, 0, 0)
    page_layout.addWidget(self.output_widget, 1, 0)

    #QFrame makes frame around widgets
    self.frame = QFrame()
    self.frame.setLayout(page_layout)

    self.eventFilter = KeyPressFilter(parent=self.output_widget)
    self.output_widget.installEventFilter(self.eventFilter)

    self._create_window()


  #handle key presses
  def keyPressEvent(self, event:QtGui.QKeyEvent):
    print("KEY PRESSED")

  def vid_text_changed(self, text):
    self.vid = text

  
  def mode_text_changed(self, mode):
    self.mode = mode


  #A static method doesn't receive any reference argument whether it is called by an instance of a class or by the class itself
  @staticmethod
  def _create_entry(name: str, update_cb, query_cb) -> QGridLayout:
      """Creates a label and lineedit for {name} with callbacks
      update_cb: method for storing the line edit's text
      query_cb: method for submitting the query request
      """

      label = QLabel(name)
      entry = QLineEdit()
      
      entry.textChanged.connect(
          lambda text: update_cb(text)
      )

      entry.returnPressed.connect(query_cb)

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
        #self.page_layout.setFocus()
        self.app.exec() #should surround in sys.exit()??

  

  def play_vid(self):
    #which widget has focus??
    #widget = self.app.focusWidget()
    #print(widget)

    #TODO: cursor shape set not working
    #QApplication.setOverrideCursor(Qt.WaitCursor)
    cursor = QCursor()
    cursor.setShape(Qt.CrossCursor)
    self.app.setOverrideCursor(QCursor(Qt.WaitCursor)) 

    #print("play_vid called, self.vid is", self.vid)

    #save aisle num for when we're adding the annotations to the db
    aisle = None

    if self.vid == None:
      print("ERROR: self.vid is None, you need to select a video to pay")
      return

    self.vid_shortname = os.path.basename(self.vid)
    print(self.vid_shortname[:5])

    #try to extract aisle number
    if (self.vid_shortname[:5] == "aisle"):
      aisle = self.vid_shortname[5]
      print(f"This vid is detected to be for aisle {aisle}")

    #find which side of aisle this video is of
    #print(self.vid_shortname.split("_")[1][:-4])
    side = 0 if self.vid_shortname.split("_")[1][:-4] == "north" else 1

    #open the video using VideoCapture obj
    cap = cv2.VideoCapture(self.vid)

    if (cap.isOpened() == False): 
      print(f"ERROR opening video file {self.vid}")
      exit()

    #get video fps
    fps = cap.get(cv2.CAP_PROP_FPS)
    print(f"{fps} frames per second")

    #get total num of frames
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

    if (fps == 0.0):
      print("ERROR: get returned 0.0 frames per second")
      exit()

    print("frame_count is", frame_count)
    duration = frame_count / fps
    print("Duration of video is", duration, "seconds")

    framectr = 0

    #create the cv2 window and give it name
    cv2.namedWindow(self.vid_shortname, cv2.WINDOW_NORMAL)

    #resize window to custom size
    cv2.resizeWindow(self.vid_shortname, 500, 900)

    #current frame
    frame = None

    def label_circle(event, x, y, flags, param):
      if event == cv2.EVENT_LBUTTONDOWN:
          cv2.circle(frame, (x, y), ANN_CIRC_RADIUS, ANN_CIRC_COLOR, 2)

          #show marked up img
          cv2.imshow(self.vid_shortname, frame)

          #save the aisle number we know from vid
          self.selected_prod.vid_aisle = aisle
          self.selected_prod.side = side

          #simply divide curr framenum by total num of frames to get distFromFront
          fractional_dist_from_front_of_aisle = framectr / frame_count

          self.selected_prod.distFromFront = fractional_dist_from_front_of_aisle

          #add the location annotation to the loc db
          self.selected_prod.add_to_subcat_loc_db()
          


    cv2.setMouseCallback(self.vid_shortname, label_circle)
    
    #read frames until video is done
    while(cap.isOpened()):
        #read a frame
        ret, frame = cap.read()

        #make sure frame read successfully
        if ret == True:
            #cv2.circle(frame, (100, 100), 3, (255, 0, 0), 2)
            #display a frame in the window
            cv2.imshow(self.vid_shortname, frame)

            key = cv2.waitKey(1)

            #can press q to quit
            if key == ord('q'):
              self.output_widget.setFocus()
              break

            #pause video using space key or p key
            if key == ord('p') or key == ord(' '):
              print("Paused at frame number %d and at time %.2f sections" % (framectr, framectr / fps))
              cv2.setWindowTitle(self.vid_shortname, SELECT_ITEM_STR)
              key = cv2.waitKey(-1) #wait until any key is pressed

              #can press q to quit while paused
              if key == ord('q'):
                self.output_widget.setFocus()
                break

              cv2.setWindowTitle(self.vid_shortname, self.vid_shortname)
                

            framectr += 1

        #something wrong opening frame, so break out of loop now
        else: 
            break
    

    #release video capture obj
    cap.release()
    
    #close all cv2 windows
    cv2.destroyAllWindows()

    #QApplication.restoreOverrideCursor()


if __name__ == "__main__":
    app = QApplication([])
    gui = AnnotatorGUI(app)
    gui.run()