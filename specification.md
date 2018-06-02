# Specification

Here will be described how should application look and feel in much detail as possible.  
Still the idea is to leave developer the space for it own expression.  
As Steve Jobs said: "We're not hiring smart people to tell them what to do, but to ask them." (something like that :) ).  
Once installed APP should run in background as we want from it to perform task - sending messages, periodically.

## Design lead
Main colours blue - dominant, white and red.  
Developer should make the rest, of course will discuss it.  

## Workflow

Now, step by step through intended APP usage.

### Starting the APP

While application is booting we have logo and application name writen on a blue background.

### After the APP is up

#### If up for the first time

Display message that will warn user about costs, something like:  
Mobile carrier will charge you for sending SMS.  
There should be an accept button.  

After this, user should see:
1. Settings button - top right corrner
2. Add another number button - top off the screen
3. List of numbers to which SMS will be sent.

#### List element
Each list element should contain:
1. Number to send SMS
2. The first line of inputed text
3. Edit button
4. Send immediatley button

For example:
Number     | Edit
First line | Send

Click on edit button should display a pop-up where user can change:
1. Time interval for sending the message (drop-down menu):
i. once a day
ii. once a week
iii. once a month
2. Change text - Text Area
3. Update button - for saving the changes
4. Remove button - for removing a number

### Add new number
Dialog similar to edit pop-up.  
Instead of Update button - Add
Instead of Remove button - Cancel

### General Settings

Let's here allow user to see the statistics:  
Number of send messages per number and total.

Defaults for number, text and period.

## Notes

Keep strings in APP in a smart way, so we can easyley add multilanguage support.
