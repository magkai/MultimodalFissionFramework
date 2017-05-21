/*
 * MIT License
 *
 * Copyright (c) 2017 Magdalena Kaiser
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.dfki.mmf.examples.messy_workingplace_example;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

/**
 * Created by Magdalena Kaiser on 10.12.2016.
 */

/**
 * A MongoDB database containing all objects and its properties used for the user study example
 */
public class MongoDBWorldDescription {

    public MongoDatabase createUserStudyDatabase() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("userstudydb");
        db.getCollection("Person").insertOne(
                new Document("worldObjectId", "Person1")
                        .append("worldobjecttype", "Person")
                        .append("name", "Magdalena")
        );
        db.getCollection("Person").insertOne(
                new Document("worldObjectId", "Person2")
                        .append("worldobjecttype", "Person")
        );
        db.getCollection("User").insertOne(
                new Document("worldObjectId", "User1")
                        .append("worldobjecttype", "User")
                        .append("name", "Magdalena")
        );
        db.getCollection("User").insertOne(
                new Document("worldObjectId", "User2")
                        .append("worldobjecttype", "User")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.8")
                                        .append("yPosition", "0.0")
                                        .append("zPosition", "0.13"))
        );
        db.getCollection("Robot").insertOne(
                new Document("worldObjectId", "Robot1")
                        .append("worldobjecttype", "Robot")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.0")
                                        .append("yPosition", "0.0")
                                        .append("zPosition", "0.0"))
                        .append("name", "Nao")
        );
        db.getCollection("Scissors").insertOne(
                new Document("worldObjectId", "Scissors1")
                        .append("worldobjecttype", "Scissors")
                        .append("color", "black")
                        .append("size", "big")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.27")
                                        .append("yPosition", "-0.85")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Scissors").insertOne(
                new Document("worldObjectId", "Scissors2")
                        .append("worldobjecttype", "Scissors")
                        .append("color", "blue and red")
                        .append("size", "small")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.6")
                                        .append("yPosition", "-0.55")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen1")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "pink")
                        .append("size", "big")
                        .append("label", "Rex Textmarker")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.68")
                                        .append("yPosition", "-0.3")
                                        .append("zPosition", "-0.12"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen2")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "yellow")
                        .append("size", "big")
                        .append("label", "Stabilo")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.39")
                                        .append("yPosition", "-0.32")
                                        .append("zPosition", "-0.12"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen3")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "pink")
                        .append("size", "small")
                        .append("label", "Stabilo")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.39")
                                        .append("yPosition", "0.26")
                                        .append("zPosition", "-0.22"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen4")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "green")
                        .append("size", "small")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.5")
                                        .append("yPosition", "-0.61")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen5")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "yellow")
                        .append("size", "big")
                        .append("label", "Pelikan")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen6")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "orange")
                        .append("size", "big")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "MarkerPen7")
                        .append("worldobjecttype", "MarkerPen")
                        .append("color", "pink")
                        .append("size", "big")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil1")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "purple")
                        .append("size", "small")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.36")
                                        .append("yPosition", "-0.58")
                                        .append("zPosition", "-0.20"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil2")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "blue")
                        .append("size", "medium")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.53")
                                        .append("yPosition", "-0.275")
                                        .append("zPosition", "-0.12"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil3")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "red")
                        .append("size", "small")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil4")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "green")
                        .append("size", "medium")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil5")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "orange")
                        .append("size", "big")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "ColoredPencil6")
                        .append("worldobjecttype", "ColoredPencil")
                        .append("color", "purple")
                        .append("size", "big")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen1")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "blue")
                        .append("label", "Mathema")
                        .append("approximate Position", "on your left")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen2")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "blue")
                        .append("label", "Mathema")
                        .append("approximate Position", "on your far left")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.32")
                                        .append("yPosition", "-0.45")
                                        .append("zPosition", "-0.12"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen3")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "yellow")
                        .append("label", "pizza")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.32")
                                        .append("yPosition", "-0.38")
                                        .append("zPosition", "-0.22"))
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen4")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "yellow")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen5")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "blue")
                        .append("approximate position", "on your far left")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen6")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "white")
                        .append("label", "KTH")
        );
        db.getCollection("Pen").insertOne(
                new Document("worldObjectId", "BallPen7")
                        .append("worldobjecttype", "BallPen")
                        .append("color", "red")
        );
        db.getCollection("Paper").insertOne(
                new Document("worldObjectId", "Paper1")
                        .append("worldobjecttype", "Paper")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.63")
                                        .append("yPosition", "0.15")
                                        .append("zPosition", "-0.2"))
        );
        db.getCollection("Plate").insertOne(
                new Document("worldObjectId", "Plate1")
                        .append("worldobjecttype", "Plate")
                        .append("material", "paper")
                        .append("color", "orange")
                        .append("motif", "white dots")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.33")
                                        .append("yPosition", "0.56")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Plate").insertOne(
                new Document("worldObjectId", "Plate2")
                        .append("worldobjecttype", "Plate")
                        .append("material", "paper")
                        .append("color", "grey white")
                        .append("motif", "zig-zag line")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.27")
                                        .append("yPosition", "-0.85")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Plate").insertOne(
                new Document("worldObjectId", "Plate3")
                        .append("worldobjecttype", "Plate")
                        .append("material", "ceramic")
                        .append("color", "white")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.6")
                                        .append("yPosition", "-0.87")
                                        .append("zPosition", "-0.12"))
        );
        db.getCollection("Cup").insertOne(
                new Document("worldObjectId", "Cup1")
                        .append("worldobjecttype", "Cup")
                        .append("print", "TECS")
                        .append("color", "white")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.45")
                                        .append("yPosition", "0.35")
                                        .append("zPosition", "-0.15"))
        );
        db.getCollection("Cup").insertOne(
                new Document("worldObjectId", "Cup2")
                        .append("worldobjecttype", "Cup")
                        .append("color", "blue")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.14")
                                        .append("yPosition", "0.48")
                                        .append("zPosition", "-0.15"))
        );

        return db;
    }
    public MongoDatabase clearDatabase(String dbName) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoIterable<String> collectionNames = db.listCollectionNames();
        for(String name: collectionNames) {
            db.getCollection(name).drop();
        }
        return db;
    }

    public void printAllDatabaseEntries(MongoDatabase db) {
        MongoIterable<String> collectionNames = db.listCollectionNames();
        for(String name: collectionNames) {
            FindIterable<Document> iterable = db.getCollection(name).find();
            iterable.forEach(new Block<Document>() {
                public void apply(final Document document) {
                    System.out.println(document.toJson());
                }
            });
            System.out.println("----------------------");
        }
    }
}
