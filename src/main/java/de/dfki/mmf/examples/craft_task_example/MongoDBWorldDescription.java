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

package de.dfki.mmf.examples.craft_task_example;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import static java.util.Arrays.asList;

/**
 * Created by Magdalena Kaiser on 11.12.2016.
 */

/**
 * A MongoDB database containing all objects and its properties used for the craft task example
 */
public class MongoDBWorldDescription {

    public MongoDatabase createCraftTaskDatabase() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("craftdb");
        db.getCollection("User").insertOne(
                new Document("worldObjectId", "User1")
                        .append("worldobjecttype", "User")
                        .append("position",
                                new Document()
                                        .append("xPosition", "1.1")
                                        .append("yPosition", "0.25")
                                        .append("zPosition", "0.2"))
                        .append("name", "Magdalena")
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
        db.getCollection("Toolbox").insertOne(
                new Document("worldObjectId", "Toolbox1")
                        .append("worldobjecttype", "Toolbox")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.12")
                                        .append("yPosition", "-0.22")
                                        .append("zPosition", "0.0"))
                        .append("content", asList(
                                new Document()
                                        .append("toolName", "Drill")
                                        .append("amount", "1"),
                                new Document()
                                        .append("toolName", "Screwdriver")
                                        .append("amount", "3"),
                                new Document()
                                        .append("toolName", "Knife")
                                        .append("amount", "5"),
                                new Document()
                                        .append("toolName", "Hammer")
                                        .append("amount", "1"),
                                new Document()
                                        .append("toolName", "Sponge")
                                        .append("amount", "2"),
                                new Document()
                                        .append("toolName", "Scissors")
                                        .append("amount", "2"))

                        )
        );
        db.getCollection("Hammer").insertOne(
                new Document("worldObjectId", "Hammer1")
                        .append("worldobjecttype", "Hammer")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.57")
                                        .append("yPosition", "-0.20")
                                        .append("zPosition", "0.03"))
                        .append("belongsTo", "Toolbox1")
        );
        db.getCollection("Scissors").insertOne(
                new Document("worldObjectId", "Scissors1")
                        .append("worldobjecttype", "Scissors")
                        .append("color", "black")
                        .append("size", "big")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.18")
                                        .append("yPosition", "0.3")
                                        .append("zPosition", "0.05"))
                        .append("belongsTo", "Toolbox1")
        );
        db.getCollection("Scissors").insertOne(
                new Document("worldObjectId", "Scissors2")
                        .append("worldobjecttype", "Scissors")
                        .append("color", "blue-red")
                        .append("size", "small")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.6")
                                        .append("yPosition", "0.25")
                                        .append("zPosition", "0.01"))
                        .append("belongsTo", "Toolbox1")
        );
        db.getCollection("Sponge").insertOne(
                new Document("worldObjectId", "Sponge1")
                        .append("worldobjecttype", "Sponge")
                        .append("color", "yellow")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.5")
                                        .append("yPosition", "0.4")
                                        .append("zPosition", "0.01"))
                        .append("belongsTo", "Toolbox1")
        );
        db.getCollection("Sponge").insertOne(
                new Document("worldObjectId", "Sponge2")
                        .append("worldobjecttype", "Sponge")
                        .append("color", "green")
                        .append("position",
                                new Document()
                                        .append("xPosition", "0.25")
                                        .append("yPosition", "-0.18")
                                        .append("zPosition", "-0.05"))
                        .append("belongsTo", "Toolbox1")
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
