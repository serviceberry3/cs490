from dataclasses import dataclass
from enum import Enum
import sqlite3
from constants import *


class Product():
    def __init__(self, prodId, name, aisle, rootCatId, rootCatName, subCatId, subCatName):
        self.prodId: int = prodId
        self.name: str = name

        #the aisle as retrieved from stop and shop's API
        self.aisle: int = aisle

        #the aisle as we know it based on actual vid recorded
        self.vid_aisle: int = None
        self.rootCatId: int = rootCatId
        self.rootCatName: str = rootCatName
        self.subCatId: int = subCatId
        self.subCatName: str = subCatName

        self.distFromFront = None

        self.absX = None
        self.absY = None

        self.side = None

        self.__conn = None
        self.__cursor = None


    #add the subcategory to the subcategory location table in the database
    def add_to_subcat_loc_db(self):
        print(f"Recording subcat {self.subCatName} in aisle {self.vid_aisle} on side {self.side}, at fractional dist {self.distFromFront} from front of aisle")

        #connect to the sqlite db
        self.__conn = sqlite3.connect(DB_PATH)
        self.__cursor = self.__conn.cursor()

        #execute the prepared statement with prepared substituter values
        self.__cursor.execute('insert into subcat_loc values (?,?,?,?,?,?,?)', (self.subCatId, self.subCatName, self.vid_aisle, self.side, self.distFromFront, self.absX, self.absY))
        self.__conn.commit()
        self.__cursor.close()

        return