from PySide6 import QtCore, QtGui, QtWidgets

SELECT_ITEM_STR = "Select item listing in main window"
WAIT_FOR_ANN_STR = "Click corresponding item in video frame"
DONE_ANN = "Press n to label another item, or space to continue video"

ANN_CIRC_RADIUS = 5
ANN_CIRC_COLOR = (0, 0, 255)

PROD_SELECT_COLOR = QtGui.QColor(100, 100, 150)


#aisle dimensions in inches
AISLE_DIST = 965
AISLE_WIDTH = 107

CLICK_IN_VIDEO_HELP_TXT = "Find item in video frame window"

DB_PATH = "/home/nodog/docs/files/YaleSenior/cs490/cs490/data_prep/scrape/output/prod.sqlite"