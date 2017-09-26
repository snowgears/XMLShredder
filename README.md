# XML Shredder

XML Shredder is a utility to parse through an xml file and import data from specified paths into a relational database. Because it is built on SAX, it is able to handle extremely large files and is very versatile. Developed by [Tanner Embry](http://tannerembry.com) while at the [Claresco Corporation](http://www.claresco.com).

Open sourced by the will of [Claresco](http://www.claresco.com).

## Getting Started

To begin using this utility, simply clone or fork the repository, setup the provided config.xml file, and run the main class (Runner) with two arguments: the path to the configuration xml file and the path to the data xml file.

For a simple example, please see below.

## Example

Here we have a simple data file:
#### books.xml
```xml
<?xml version="1.0"?>
<catalog>
   <book id="bk101">
      <author>Gambardella, Matthew</author>
      <title>XML Developers Guide</title>
      <genre>Computer</genre>
      <price>44.95</price>
      <publish_date>2000-10-01</publish_date>
      <description>An in-depth look at creating applications
      with XML.</description>
   </book>
   <book id="bk102">
      <author>Ralls, Kim</author>
      <title>Midnight Rain</title>
      <genre>Fantasy</genre>
      <price>5.95</price>
      <publish_date>2000-12-16</publish_date>
      <description>A former architect battles corporate zombies,
      an evil sorceress, and her own childhood to become queen
      of the world.</description>
   </book>
   <book id="bk103">
      <author>Corets, Eva</author>
      <title>Maeve Ascendant</title>
      <genre>Fantasy</genre>
      <price>5.95</price>
      <publish_date>2000-11-17</publish_date>
      <description>After the collapse of a nanotechnology
      society in England, the young survivors lay the
      foundation for a new society.</description>
   </book>
   <book id="bk104">
      <author>Corets, Eva</author>
      <title>Oberons Legacy</title>
      <genre>Fantasy</genre>
      <price>5.95</price>
      <publish_date>2001-03-10</publish_date>
      <description>In post-apocalypse England, the mysterious
      agent known only as Oberon helps to create a new life
      for the inhabitants of London. Sequel to Maeve
      Ascendant.</description>
   </book>
   <book id="bk105">
      <author>Corets, Eva</author>
      <title>The Sundered Grail</title>
      <genre>Fantasy</genre>
      <price>5.95</price>
      <publish_date>2001-09-10</publish_date>
      <description>The two daughters of Maeve, half-sisters,
      battle one another for control of England. Sequel to
      Oberon's Legacy.</description>
   </book>
</catalog>
```

And the relational database we will be importing data into contains this table named book:
#### book

| id   | author | title | genre  |
| ------------- |:-------------:| -----:| -----:|
|      |       |       |       |
|      |       |       |       |        |


#### config.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
  <connection>
    <username>username</username>
    <password>password</password>
    <host>jdbc:postgresql://example:1212/exampledb?useUnicode=true;characterEncoding=UTF-8;stringtype=unspecified</host>
  </connection>
  <mapping> <!-- Parent mappings should be unique fields of data that other mappings (that may not be unique) can attach to-->
    <xpath>book</xpath> <!-- This can either be a full path or partial path. As long as there are unique entries -->
    <xattribute>id</xattribute>  <!-- This field will be blank if reading directly out of xml tags -->
    <dbtable>book</dbtable>
    <dbfield>id</dbfield>
    <child_mapping> <!-- Child mapping means that it is tied to the parent mapping when being inserted into database -->
      <xpath>catalog.book.author</xpath> <!-- A more specific mapping path example -->
      <xattribute></xattribute>
      <dbtable>book</dbtable>
      <dbfield>author</dbfield>
    </child_mapping>
    <child_mapping>
      <xpath>catalog.book.title</xpath>
      <xattribute></xattribute>
      <dbtable>book</dbtable>
      <dbfield>title</dbfield>
    </child_mapping>
    <child_mapping>
      <xpath>catalog.book.genre</xpath>
      <xattribute></xattribute>
      <dbtable>book</dbtable>
      <dbfield>genre</dbfield>
    </child_mapping>
  </mapping>
</config>
```

This defines a **parent mapping** for the book's **id** and will populate that entry into the database with the **child mappings** of **author**, **title**, and **genre** associated with that **id**.

Multiple parent mappings can be defined, each with their own child mappings but the utility is not recursive. It is only meant to pull out *object entries* in a sense, that can be inserted/updated in the database.

#### Queries executed when running the utility with this example:
(If there are no existing entries in the database, the utility will run insert queries)
```sql
insert into book (id, author, title, genre) values ('bk101', 'Gambardella, Matthew', 'XML Developers Guide', 'Computer')
insert into book (id, author, title, genre) values ('bk102', 'Ralls, Kim', 'Midnight Rain', 'Fantasy')
insert into book (id, author, title, genre) values ('bk103', 'Corets, Eva', 'Maeve Ascendant', 'Fantasy')
insert into book (id, author, title, genre) values ('bk104', 'Corets, Eva', 'Oberons Legacy', 'Fantasy')
insert into book (id, author, title, genre) values ('bk105', 'Corets, Eva', 'The Sundered Grail', 'Fantasy')
```
(If there are existing entries in the database, the utility will run update queries)
```sql
update book set author='Gambardella, Matthew', title='XML Developers Guide', genre='Computer' where id='bk101'
update book set author='Ralls, Kim', title='Midnight Rain', genre='Fantasy' where id='bk102'
update book set author='Corets, Eva', title='Maeve Ascendant', genre='Fantasy' where id='bk103'
update book set author='Corets, Eva', title='Oberons Legacy', genre='Fantasy' where id='bk104'
update book set author='Corets, Eva', title='The Sundered Grail', genre='Fantasy' where id='bk105'
```

## Copyright and License

Copyright (c) 2017 Claresco Corp.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
