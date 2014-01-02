Android-Weather-Search-App
==========================

The Weather Search app uses the Yahoo weather RSS feed to output the current weather conditions and weather forecast for the next 5 days
for the location entered by the user. The user can enter the location as either the zip code or the name of the location.
The RSS feed is converted into xml format by a php script running on Amazon's cloud server, which is then converted into 
JSON string by a java servlet. The app retrieves this JSON string asynchronously and displays the details to the user.
The app also provides functionality to the user to post and share the weather information to user's facebook profile.
