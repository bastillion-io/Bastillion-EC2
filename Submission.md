## Which Categories Best Fit Your Submission and Why?

Lets try for ....

"Best New Monkey", "Best Usability Enhancement", and "Best Contribution to Operational Tools, Availability, and Manageability"

A lot of times with clustered instances, administrators do their best to keep them the same.  And even doing system
level patching on non-clustered instances, administrators will likely be running the same patches. So why not give them
a way to share commands across their sessions?

Also, one of the more interesting aspects, is that b/c all the SSH communications pass through a central web server, all
activity can be logged at that single location.  Auditing and traceability can become much easier b/c it can be done from
that single location. No more jumping from server to server, hunting down history logs, and trying to determine who has
logged in.

I think it could be particularly useful in a private-virtualization scenario ("Private Cloud"), where the instances are
kept protected in a perimeter network of some sort.  With something like this, you could allow limited administrative
access to the instances in the perimeter network without exposing port 22.


## Describe your Submission

A web-based ssh console to execute commands and manage multiple EC2 instances simultaneously running on Amazon Web
Services (AWS). EC2Box allows you to share terminal commands and upload files to all your EC2 instances. Once the
sessions have been opened you can select a single EC2 instance or any combination to run your commands.  Also,
additional instance administrators can be added and their terminal sessions and history can be audited.

## Provide Links to Github Repo's for your Submission

https://github.com/skavanagh/EC2Box

