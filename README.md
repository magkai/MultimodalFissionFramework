# Multimodal Fission Framework
A framework written in Java for realizing Multimodal Fission (MMF) with a focus on the area of collaborative Human-Robot Interaction. 
The framework has been developed in the scope of my master thesis at Saarland University and at DFKI GmbH.
It has been developed independently of a specific multimodal system with the aim to be connectable to various existing dialog managers in an easy manner. An adapter needs to be implemented which translates the output of the dialog manager into the required predicate input of the MMF framework.
The framework uses Apache Maven as its build system.

## Main information about the MMF framework ##

**Input and Output:**
- The MMF framework receives a semantic predicate as input, which states in an abstract form the information to present to a user.
- The predicate input uses the well-defined predicate structure of predicates in the artificial language Lojban (www.lojban.org). 
- SimpleNLG (https://github.com/simplenlg/simplenlg) is used for generating simple sentences from the predicate input.
- Context information can currently be provided as an OWL ontology (https://www.w3.org/TR/owl-ref/) or as entries in a MongoDB database (https://www.mongodb.com/). The MMF framework stores the information internally as JSON Objects.
- The framework outputs a plan containing the selected modalities and devices for each output element.
- An execution component is provided to execute the generated plan concurrently on the connected physical devices.

**Available Modalities:**
- Speech
- Pointing
- Gaze
- Image Displaying
- Waving
- Nodding/Headshaking
- Further modalities can be added

**Modality and Device Selection:**
- The modality and device selection are formulated as constraint optimization problems. 
- OptaPlanner (http://www.optaplanner.org/) is used to solve these constraint optimization problems.
- The implemented criteria are tailored to the area of Human-Robot Interaction. The framework can easily be extended by new criteria.

**Object Reference Generation:**
- Multimodal References can be generated.
- An algorithm for generating verbal object references by extracting salient object attributes is included (see src/main/java/de/dfki/mmf/attributeselection).

**Example Scenarios:**
Three example scenarios are implemented (see src/main/java/de/dfki/mmf/examples/).


## License of Multimodal Fission Framework ##
The Multimodal Fission Framework is licensed under the terms and conditions of the MIT License.
