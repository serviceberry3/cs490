import pandas as pd
import json
import sqlite3
import os


PRODS_FOLDER = "../products"
keys_of_interest = ["prodId", "name", "aisle", "regularPrice", "upc", "rootCatId", "rootCatSeq", "rootCatName", "productCategoryId", "subcatId", "subcatName"]

class BuildDatabase:
    """
        Class for creating new database
    """
    def __init__(self, database_name, termcode=None):
        #load_dotenv()
        self.__conn = None
        self.__cursor = None
        self.__database_name = database_name

    def prep_database(self):
        """
            Method for database preparation
        """
        database_path = "output/" + self.__database_name + ".db"

        #remove old db file if it exists
        if os.path.exists(database_path):
            print(f"stale db {database_path} found, deleting now")
            os.remove(database_path)

        self.__conn = sqlite3.connect(database_path)

        #create all of the new tables we need
        self.create_tables()

        #parse JSON files and insert the data
        self.insert_data()


    def create_tables(self):
        """
            Method for creating tables from schema in create_tables.txt
        """

        #get sqlite3 cursor
        self.__cursor = self.__conn.cursor()

        try:
            with open('create_tables.txt', 'r', encoding='UTF-8') as file:
                sql = ""

                for line in file:
                    if len(line) > 1:
                        #print(f"adding line {line} to sql")
                        sql += line
                    else:
                        print(f"executing the following sql now:\n{sql}")
                        self.__cursor.execute(sql)
                        sql = ""

            #commit changes and close sqlite3 db connection
            self.__conn.commit()
            self.__cursor.close()

        except RuntimeError:
            print("Database creation FAILED")


    


    def insert_data(self):
        """
            Method for inserting data from all stop and shop JSON product files into database
        """
        self.__cursor = self.__conn.cursor()

        # clear table data
        #self.__cursor.execute('delete from products')

        for filename in absoluteFilePaths(PRODS_FOLDER):
            with open(filename) as f:
                print(f"attempting load file {filename} as json")

                #turn json text into a Python dict
                data = json.load(f)
                this_prod = data["response"]["products"][0]

                res = {}

                for key in keys_of_interest:
                    if key in this_prod:
                        #print(f"{key}: {this_prod[key]}")
                        res[key] = this_prod[key]
                    else:
                        res[key] = None

            self.__cursor.execute('insert into products values (?,?,?,?,?,?,?,?,?,?,?)', (res["prodId"], res["name"], res["aisle"], res["regularPrice"], res["upc"], res["rootCatId"], res["rootCatSeq"], res["rootCatName"], res["productCategoryId"], res["subcatId"], res["subcatName"]))


        self.__conn.commit()
        self.__cursor.close()


def absoluteFilePaths(directory):
        for dirpath, _, filenames in os.walk(directory):
            for f in filenames:
                yield os.path.abspath(os.path.join(dirpath, f))


def test_json_parse():
    keys = ["prodId", "name", "aisle", "regularPrice", "upc", "rootCatId", "rootCatSeq", "rootCatName", "productCategoryId", "subcatId", "subcatName"]

    # Open JSON data
    with open("../products/000232.json") as f:
        #turn json text into a Python dict
        data = json.load(f)
        #d = pd.read_json(f)
        #data = d.json()

    # Create A DataFrame From the JSON Data
    #df = pd.DataFrame(data)
    #print(df)


    #print(df.columns)

    #print(df.head())

    this_prod = data["response"]["products"][0]

    for key in keys:
        if key in this_prod:
            print(f"{key}: {this_prod[key]}")



if __name__ == '__main__':
    #test_json_parse()
    BuildDatabase(database_name="prod").prep_database()
    '''
    for name in absoluteFilePaths("."):
        print(name)
    '''