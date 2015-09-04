# Pojomizer
A simple Java lib to convert POJOs to CSV format.
Developed as an interview challenge.

## Design considerations
The challenge specification can be found on [desafio_java.pdf] (desafio_java.pdf) (portuguese). 
It leaves a few points open, and I was instructed to follow my heart :) 
So, here goes my take on it:
- What is an attribute: This an OOP concept that is not specifically defined in Java, and can be interpreted as 'property', 'field', or even 'public field. I have defined it as any data field of a class.   
- How to handle a list of POJOs of different classes: One approach would be to throw an error. I choose to add columns for every attribute found, reusing columns for fields with the same name but from different classes. This may result on a column having values of different types (eg, double and text), which may or may not be useful.   
- What is a complex object: does String count? What about enums and primitive wrappers? I choose to support primitive types, their wrappers, String and enums.
- How to handle *null* values: One might write *"null"*, or an empty string. I went with the empty string.

The class diagram can be found [class diagram.png](class diagram.png)

## Building
You must have Maven and a JDK (minimum 1.7) installed.
As a Maven project, your IDE should be able to handle the building process. 
Or, on a terminal, go to the project root and type: `mvn clean install`
