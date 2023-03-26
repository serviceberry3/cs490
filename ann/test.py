from PySide6.QtWidgets import *
from PySide6.QtCore import *
from PySide6.QtGui import *

class Window(QWidget):

    def __init__(self, parent=None):
        super().__init__(parent=parent)
        self.layout = QHBoxLayout(self)
        
        self.label1 = QLabel("", self)
        self.label2 = QLabel("Key Pressed: ", self)
        self.layout.addWidget(self.label2)
        self.layout.addWidget(self.label1)
        self.resize(100, 50)


        self.eventFilter = KeyPressFilter(parent=self)
        self.installEventFilter(self.eventFilter)

class KeyPressFilter(QObject):

    def eventFilter(self, widget, event):
        if event.type() == QEvent.KeyPress:
            print("KEY PRESSED")
            text = event.text()
            if event.modifiers():
                text = event.keyCombination().key().name.decode(encoding="utf-8")

            if (text == 'q'):
                print("quitting")
                exit()
            widget.label1.setText(text)
        return False



if __name__ == '__main__':
    app = QApplication([])
    window = Window()
    window.show()
    app.exec()