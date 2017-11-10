# Alvex manager dashboard tasks

This project is a custom version of the original [Alvex manager dashboard tasks](https://github.com/ITDSystems/alvex-manager-dashboard-tasks) project created and maintained by [ITDSystems](http://www.itdhq.com/en)

We decided to create this version because we had the requirement to have some Alfresco users to monitor others users' workflow tasks inside Alfresco Share, and that is exactly what the original project does, but at the cost of adding other Alvex dependencies that had no use for our specific use case.
In order to avoid adding those, we thought that it would be better to create a single project containing all the required pieces to have such feature available, and nothing more than that.

So, the idea is not to replace the original project, but just to be a smaller and easier way to deploy the solution.
Alvex is a more complex and complete project, and if you need its full set of features, get the original project, and not this one.

Basically, what we did was to get parts of the following projects, all listed dependencies in the original project:
* https://github.com/ITDSystems/alvex-manager-dashboard-tasks
* https://github.com/ITDSystems/alvex-utils
* https://github.com/ITDSystems/alvex-orgchart

The removed dependencies are:
* https://github.com/jankotek/mapdb/
* https://github.com/ITDSystems/alvex-project-management
* https://github.com/ITDSystems/alvex-workflow-permissions
* https://github.com/ITDSystems/alvex-datagrid
* https://github.com/ITDSystems/alvex-masterdata
* https://github.com/ITDSystems/alvex-uploader

# Few things to notice

This first version was developed and tested against Alfresco Community 5.0.d, and so far, has not been tested with other versions.
Feel free to test it and report any issues you may find.

Alfresco SDK 3.0.1 was used to create this project.

# How to use the project

You have two options:
* Get the source code and package it using Maven just like you do with any Alfresco SDK based project, or
* Get the ready to use AMP file in the [releases page](https://github.com/douglascrp/alvex-manager-dashboard-tasks-standalone-repo/releases).

This is the **repository** part of the project, which means you should deploy the generated AMP file in to the **alfresco.war**.

For the share part, please get the following project https://github.com/douglascrp/alvex-manager-dashboard-tasks-standalone-share

# Collaboration

If you find any problem with this project, please, let us know, but if you can fix it and send us a PR, it would be even better.

Any kind of improvement we make on this project is going to be send to the original creators, as we have already been doing since we started using the project.