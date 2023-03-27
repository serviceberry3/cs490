"""Utilities file for a custom QDialog and a fixed-width font."""

from PySide6.QtWidgets import QDialog, QDialogButtonBox, QTextEdit, QVBoxLayout
from PySide6.QtGui import QFont

# Named constant for a fixed-width font, Monaco (if it is available on the system).
# Otherwise, it will pick a "TypeWriter" style with a fixed pitch.
FW_FONT = QFont("Monaco")
FW_FONT.setStyleHint(QFont.StyleHint.TypeWriter)
FW_FONT.setFixedPitch(True)

class FixedWidthMessageDialog(QDialog):
    """Custom subclass of QDialog that displays a message in a fixed-width font and a single
        button, 'OK'.
    """

    def __init__(self, title, message, parent=None):
        """Initializer for FixedWidthMessageDialog."""

        # Call the QDialog (superclass) initializer
        super().__init__(parent)

        # Set the window title
        self.setWindowTitle(title)

        # Initialize the button widget, ready to add to the layout
        button = QDialogButtonBox(QDialogButtonBox.StandardButton.Ok)
        button.accepted.connect(self.accept)

        # Initialize and lay out the message box, which must be sized to the message displayed
        msg_widget = QTextEdit()
        msg_widget.setPlainText(message)
        msg_widget.setFont(FW_FONT)
        msg_widget.setReadOnly(True)
        msg_widget.setLineWrapMode(QTextEdit.LineWrapMode.NoWrap)
        msg_size = msg_widget.document().size().toSize()
        msg_widget.setFixedSize(msg_size)

        # Set this dialog's layout to be a VBox (vertically-aligned widgets), and add the message
        # widget and the button to the layout
        self.layout = QVBoxLayout()
        self.layout.addWidget(msg_widget)
        self.layout.addWidget(button)

        # Set self.layout to be the VBox layout containing the two widgets
        self.setLayout(self.layout)