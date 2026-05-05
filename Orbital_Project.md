# Orbital Project

Maximise your Practice Papers / Assignments / Tutorials by offloading your note taking to us.

Start paper with the app, Paste in questions that are hard, difficult or unique. Then type a rough sketch of what you learnt from those questions (after going through answers, or after seeking advice from mentor, …). AI will collate all the rough notes and convert them into solid quality concise notes. Our app will convert them into flashcards for you to review when revising.

Note that the storage of these flashcards are local, but requires a server to store publicly shared flashcards (more on that later). So it will need an authentication feature on this app (to create accounts)

# Intelligent Features

1. Coursework Flashcard (CFC) generation (Supported by AI) based on user’s rough notes and inputted questions from their coursework  
   1. Rough notes → AI formats it nicely and readable (add elaboration where needed) → Create assignment specific coursework flashcard  
   2. While CFC is being generated, also define the topic of the current “rough notes” that is being analysed. The CFC meta data should contain this information for later use. Also should contain **potentially related topics**

2. Use segregated topical data (obtained via metadata from CFC) to create Topical flashcards TFC for user’s mods/subjects  
   1. Basically for a particular topic, sieve out topical notes from each and every CFC, to create a topical flashcard  
   2. Ultimately user can view topical flashcard and/or coursework flashcard  
   3. More about the process later

3. Market place for users to upload their TFCs online for sharing. Other users can import TFC and topical notes into their own database.  
   1. Users can then look through the imported notes to vet them. Should they be comfortable with it, they can choose to merge the imported notes’ data into their existing database. This way they can complement their learning with others

4. More on marketplace → Split into 2 “markets”, 1 public flashcard sharing place, and 1 private (only with friends. We therefore have a friends list feature as well).   
   1. Private Marketplace ⇒ Users can readily share notes via the private market place (friends only). In this private place, note sharing is a lot more streamlined and is not monetised)  
   2. Public Marketplace ⇒ A monetised (optional ie users could upload their notes for free as well) marketplace where users can upload their notes. Uploaded notes can be then be purchased by other users and merged into their database should they wish to do so as described in point 3
            → This public marketplace can make incentive for Tuition centres or other entities to also start uploading their notes. Increasing the available quality of notes
      1. THIS IS STILL BEING IDEATED, WE NEED IDEAS TO INCENTIVISE PEOPLE TO UPLOAD THEIR HARD EARNED NOTES  
   3. Public marketplace should also have a likes/upvote feature. This can help users to filter out badly made notes using crowd-sourced rating   
      1.    
   4. Also more on marketplace → Put a search feature, so can filter based on mods / topics

5. Topical Mindmap. Basically an interactive UI showing our topical flashcards (TFC). We also will connect topical flashcards that are potentially linked to one another using a line. On that line, we have a button showing AI insights, → Once pressed we receive AI generated insights of those topics (More on that later)  
 


## Step by step process of making flash cards

1. Upload SS to app \+ notes made on what went wrong for the question / new ideas learnt  
2. Use AI API to convert into Coursework Flashcard based on the Assignment code/paper  
   1. At the same time ask the AI to format the metadata in the flashcard into a exportable format  
   2. Each CFC contains header information / metadata on the flashcard note. 
   3. Below is the structure segregated into its layers. This is also how the storage of the notes will look like. (It will be in a tree structure)
      1. Root → will be the userID
         2. Children of Root (2nd layer) → Year \+ semester of study (or just year if semester is not relevant)
            3. Children of the  2nd layer (3rd layer) → CourseCodes for Subjects in that Year and semester of study
               4. Only 2 Children of the 3rd layer (4th layer) → Left child is CFCs, Right Child is TFCs
                  5. Children of the 4th layer LEFT child (5th layer LEFT) ↦ Children of CFCs → Course Work done by student. (Tutorials, Practice Papers, Assignments, Quizes, Midterms, etc)
                  5. Children of the 4th layer RIGHT child (5th layer RIGHT) ↦ Children of TFCs → Nodes for Topics in the course
                     6. Children of the 5th layer LEFT (6th layer RIGHT ) → These will be leaves containing the actual CFCs
                     6. Children of the 5th layer RIGHT (6th layer RIGHT ) → Will be the nodes for each subtopic of the parent topic node
                        7. Children of the 6th layer RIGHT → These will be leaves containing the actual TFCs  
3. Use the exported format to build our database   
4. Then convert the database into readable, topical notes for the user  
   1. Contains topical flashcards. → These flashcards allow for linking between concepts learnt per topic amongst the Coursework flashcards  
5. Using the database → We create link map UI between topical flashcard notes (nodes)  
   1. The link will essentially be an AI insight between possible overlaps of topics etc  
   2. This should be A clickable button which will prompt the creation of the insight → that way we do not have to generate insights on EVERY topic as that could overload the system ⇒ Optimisation.   
      1. That way AI will help us make proper insight of the two  
         1. Insights wil be structures as:  
            1. Possible questions testing on concepts merged from these topics  
            2. …  
   3. Not all nodes have “links” inbetween them. (Only between related topics decided by ↓)  
   4. Each TFC has header information containing (Curr topics \+ possibly related topics)  
      1. From there only we do the connections through code and for each link we embed the ai insight button

## First deliverable (technincal proof of concept)
- A fully integrated working system of both frontend and backend
   - to be done for a relatively trivial feature → sign-up and login

## Next steps after
- Authentication for an account (password \+ email)  
- Create a friends list   
- Make a secure sharing network to implement the sharing of flashcards  
  - Marketplace  
  - For publicSpace, implement a secure payment method