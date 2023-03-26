from dataclasses import dataclass
from enum import Enum

@dataclass
class QueryArgs:
    """Collection of arguments for query calls"""
    prodId: int
    name: str
    subCatName: str

    def set_prodid(self, id):
        self.prodId = id

    def set_prodname(self, text):
        """Set product name"""
        self.name = text

    def set_subcat(self, text):
        """Set product's subcategory (i.e. Sandwich Cookies)"""
        self.subCatName = text


class QueryType(Enum):
    """Distinguishes which query to perform"""
    DEFAULT = 1


@dataclass
class Query:
    """
    Communication between client and server contains a
    Enumerated query_type: allows server to determine which query to make
    Dataclass query_args: allows server to fetch args
    """
    #the class properties for dataclass are automatically taken to be params of constructor
    #(the dataclass decorator auto-adds methods, e.g. the __init__() constructor)
    query_args: QueryArgs