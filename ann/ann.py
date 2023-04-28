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



VIDS_DIR = "/home/nodog/docs/files/YaleSenior/cs490/cs490/data_prep/vids/0424/"
STORE_ELEMENTS_LIST = ["entrance", "entrance_island", "exit", "checkout", "old_produce_island", "chips_wall_of_value", "produce_island_front_west", "produce_island_front_east", "floral_island", "produce_island_1", "produce_island_2", 
"produce_island_3", "produce_island_4", "produce_island_5", "produce_island_6", "produce_wall_west", "produce_wall_east", "meat_island_north", "meat_island_south", "deli", "dips_wall", "cakes_fridge_wall", "bread_wall", "fish_wall", "fish_fridge_wall", 
"wall_meat_packaged", "wall_dairy_yog_milk", "wall_dairy_egg_cheese", "aisle_ice_cream_frozen", "aisle_frozen_16", "aisle_frozen_16", "aisle_16_15", "aisle_15_14", "aisle_14_13", "aisle_13_12", "aisle_12_11", "aisle_11_10", "aisle_10_9", "aisle_9_8", "aisle_8_7", "aisle_7_6", 
"aisle_6_5", "aisle_5_4", "aisle_4_3", "aisle_3_deli", "bread_island", "grabngo", "cheese_island", "breakfast", "grabngo_deli_island", "pastries_south", "pastries_north", "alcohol", "alcohol_wall_front", "donuts_wall_front"]
DIRS = ["north", "south", "east", "west"]

def absoluteFilePaths(directory):
    for dirpath, _, filenames in os.walk(directory):
        for f in filenames:
            #yield suspends function’s execution and sends value back to caller, but retains state to enable fxn to resume where left off. When the function resumes, it continues execution immediately after the last yield run. 
            #This allows it to produce series of values over time, rather than computing them at once and sending them back like a list.
            yield os.path.abspath(os.path.join(dirpath, f))

def getFileNamesFromDir(directory):
    return [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]


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

    #get directory containing the videos
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

    #which side of store element are we annotating?
    self.side = DIRS[0][0]

    #which direction of travel was vid taken?
    self.dir = DIRS[0][0]

    #name of store element that user is labeling (ie an aisle or produce island)
    self.store_element_name = None

    #photo or video?
    self.media_type = "photo"

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

      #clear the original light blue selection that's used when browsing product results list
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

    #create gridlayout for the video dropdown areamport isfile, join
    vids_section = QGridLayout()

    vid_dropdown_txt = QLabel("Select media:")

    #dropdown menu to select which video to play
    vids_dropdown = QComboBox()
    #vids_dropdown.addItems(absoluteFilePaths(VIDS_DIR))
    vids_dropdown.addItems(getFileNamesFromDir(VIDS_DIR))
    vids_dropdown.currentTextChanged.connect(self.vid_text_changed)

    media_type_dropdown_txt = QLabel("       Type:")
    media_type_dropdown = QComboBox()

    media_type_dropdown.addItems(["photo", "video"])
    media_type_dropdown.currentTextChanged.connect(self.media_type_text_changed)

    side_dropdown_txt = QLabel("       Side:")
    side_dropdown = QComboBox()

    side_dropdown.addItems(DIRS)
    side_dropdown.currentTextChanged.connect(self.side_text_changed)

    dir_dropdown_txt = QLabel("       Dir of travel:")
    dir_dropdown = QComboBox()

    dir_dropdown.addItems(DIRS)
    dir_dropdown.currentTextChanged.connect(self.dir_text_changed)

    element_dropdown_txt = QLabel("   Store element:")
    element_dropdown = QComboBox()

    element_dropdown.addItems(STORE_ELEMENTS_LIST)
    element_dropdown.currentTextChanged.connect(self.element_text_changed)

    vids_section.addWidget(vid_dropdown_txt, 0, 0)
    vids_section.addWidget(vids_dropdown, 0, 1)
    vids_section.addWidget(media_type_dropdown_txt, 0, 2)
    vids_section.addWidget(media_type_dropdown, 0, 3)
    vids_section.addWidget(side_dropdown_txt, 0, 4)
    vids_section.addWidget(side_dropdown, 0, 5)
    vids_section.addWidget(dir_dropdown_txt, 0, 6)
    vids_section.addWidget(dir_dropdown, 0, 7)
    vids_section.addWidget(element_dropdown_txt, 0, 8)
    vids_section.addWidget(element_dropdown, 0, 9)
    

    #create gridlayout for the mode dropdown area
    mode_section = QGridLayout()

    mode_dropdown_txt = QLabel("I am labeling:")

    #dropdown menu to select whether to annotate using subcategory or specific product
    mode_dropdown = QComboBox()
    mode_dropdown.addItems(["subcategories (i.e. Sandwich Cookies)", "specific products"])
    mode_dropdown.currentTextChanged.connect(self.mode_text_changed)

    
    mode_section.addWidget(mode_dropdown_txt, 0, 0)
    mode_section.addWidget(mode_dropdown, 0, 1)
    mode_section.setHorizontalSpacing(-30)

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
    self.vid = VIDS_DIR + text

  def mode_text_changed(self, mode):
    self.mode = mode

  def side_text_changed(self, text):
    self.side = text[0]

  def dir_text_changed(self, text):
    self.dir = text[0]

  def media_type_text_changed(self, text):
    self.media_type = text

  def element_text_changed(self, text):
    self.store_element_name = text


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

    if self.vid == None:
      print("ERROR: self.vid is None, you need to select a video or photo to annotate")
      return

    self.vid_shortname = os.path.basename(self.vid)
    print(self.vid_shortname[:5])

    #try to extract aisle number
    '''
    if (self.vid_shortname[:5] == "aisle"):
      aisle = self.vid_shortname[5]
      print(f"This vid is detected to be for aisle {aisle}")'''

    #find which side of aisle this video is of
    #print(self.vid_shortname.split("_")[1][:-4])
    #side = 0 if self.vid_shortname.split("_")[1][:-4] == "north" else 1

    framectr = 0
    frame = None #current frame

    image = None #image for photo

    if self.media_type == "photo":
      pass
    else:
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


    #create the cv2 window and give it name
    cv2.namedWindow(self.vid_shortname, cv2.WINDOW_NORMAL)

    #resize window to custom size
    cv2.resizeWindow(self.vid_shortname, 500, 900)


    #callback when user clicks in frame to add a label
    def label_circle(event, x, y, flags, param):
      if event == cv2.EVENT_LBUTTONDOWN:
          if self.media_type == "photo":
            cv2.circle(image, (x, y), ANN_CIRC_RADIUS, ANN_CIRC_COLOR, 2)

            #show marked up img
            cv2.imshow(self.vid_shortname, image)

            width = image.shape[1]

            print(f"for photo: clicked at {x} and image is {width} wide")

            #assuming landscape images, just divide x val of click by width of img to get distFromStart
            fractional_dist_from_front_of_aisle = x / width

          else:
            cv2.circle(frame, (x, y), ANN_CIRC_RADIUS, ANN_CIRC_COLOR, 2)

            #show marked up img
            cv2.imshow(self.vid_shortname, frame)

            #simply divide curr framenum by total num of frames to get distFromFront
            fractional_dist_from_front_of_aisle = framectr / frame_count

          self.selected_prod.store_element_name = self.store_element_name
          self.selected_prod.side = self.side
          self.selected_prod.dir = self.dir
          self.selected_prod.distFromFront = fractional_dist_from_front_of_aisle

          #add the location annotation to the loc db
          self.selected_prod.add_to_subcat_loc_db()
          

    cv2.setMouseCallback(self.vid_shortname, label_circle)
    

    if self.media_type == "photo":
      image = cv2.imread(self.vid)
      cv2.imshow(self.vid_shortname, image)


    else:
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