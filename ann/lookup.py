"""
Processes args into SQL command and queries data
"""
from sqlite3 import connect
from contextlib import closing
import sqlite3
import sys
from communication import QueryArgs

from constants import *

class DataLookup:
    """Processes args into SQL command and queries data"""
    #if strict is True, the SQL query will match the passed prepared string exactly (ie, will match the passed product ID exactly instead of finding all products whose ID contains the passed ID number)
    def __init__(self, args: QueryArgs, strict = False):
        #the QueryArgs. 
        #QueryArgs just has some strings as properties, like product name, subcategory name, etc.
        self.args = args

        #underscore before properties indicates private var
        self._data = None
        self._query_string = ""
        self._prepared_values = []

        self.strict = strict

        self.run_query()

    #in Python, @property decorator is used to indicate that saying something like <RegLookup instance>.data will automatically call this getter function
    #the underscore in self._data indicates private var
    @property
    def data(self):
        """Returns the data in from RegLookup query"""
        return self._data


    #lookup grocyer data
    def run_query(self):
        """Manages the execution of the SQL query"""

        try:
            #connect to the db file and get SQL cursor
            with connect("file:" + DB_PATH + "?mode=ro", uri=True) as conn:
                with closing(conn.cursor()) as crsr:
                    #generate the SQL query statement
                    self.generate_query()

                    #at this pt self._query_string and self._prepared_values should be populated appropriately

                    #execute prepared statement with the appropriate prepared vals
                    crsr.execute(self._query_string, self._prepared_values)

                    #save results data
                    self._data = crsr.fetchall()

        except sqlite3.DatabaseError as ex:
            print("SQLITE ERROR:")
            print(ex, file=sys.stderr)
            sys.exit(1)


    #generate SQL query statement string
    def generate_query(self):
        """Generates the SQL query string based on user query args"""

        #the base SQL query string
        self._query_string = """SELECT cast(prodId as text), name, cast(aisle as text), \
                        cast(rootCatId as text), rootCatName, cast(subCatId as text), subCatName FROM products """

        #filter selection based on user-filled search bar fields
        #vars() returns an instance of a class as a dict, keys are object property names and values are the property values
        filters = vars(self.args)

        #clear out prepared statement inserts
        self._prepared_values = []

        first_clause = True

        #go through the query args
        for filter_name in filters.keys():
            #if the arg has a value
            if filters[filter_name] is not None:

                #all clauses except first clause require "AND" at beginning
                if not first_clause:
                    self._query_string += 'AND '

                #it's the first clause, so add the WHERE
                else:
                    first_clause = False
                    self._query_string += 'WHERE '

                self._query_string += filter_name
                self._query_string += " LIKE ? "

                prepared_str = f"{filters[filter_name]}" if self.strict else f"%{filters[filter_name]}%"

                #save prepared filter value in prepared statement list
                self._prepared_values.append(prepared_str) #remember we to prepend and append wildcards to prepared insertion

        #order results first by subcategory ID, then by aisle, then by main category ID, then by product ID
        self._query_string += "ORDER BY subCatId ASC, aisle ASC, rootCatId ASC, prodId ASC;"