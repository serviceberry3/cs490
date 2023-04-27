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

        #the aisle or store element as we know it based on user's actual selection
        self.store_element_name: str = None
        self.rootCatId: int = rootCatId
        self.rootCatName: str = rootCatName
        self.subCatId: int = subCatId
        self.subCatName: str = subCatName

        self.distFromFront = None

        self.absX = None
        self.absY = None

        self.side: str = None
        self.dir: str = None

        self.__conn = None
        self.__cursor = None


    #add the subcategory to the subcategory location table in the database
    def add_to_subcat_loc_db(self):
        print(f"Recording subcat {self.subCatName} in element {self.store_element_name} on side {self.side}, moving direction {self.dir}, at fractional dist {self.distFromFront} from start")

        #connect to the sqlite db
        self.__conn = sqlite3.connect(DB_PATH)
        self.__cursor = self.__conn.cursor()

        #execute the prepared statement with prepared substituter values
        self.__cursor.execute('insert into subcat_loc values (?,?,?,?,?,?,?,?,?)', (self.subCatId, self.subCatName, self.store_element_name, self.side, self.distFromFront, self.absX, self.absY, None, self.dir))
        self.__conn.commit()
        self.__cursor.close()

        return