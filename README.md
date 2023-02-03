Given a closed world assumption (i.e.: no dynamic classloading)
scan the rules at compile time,
to determine all possible classes (and subclasses, and implementations) used in all the rules.

The exercise is currently relying on the indexing of the current project.
If the domain model objects is coming from a dependency,
for instance some domain classes used in the Rules are defined in a transitive dependency of this project,
it would require to update the Jandex scan, to account for it (see pom.xml).

Please refer to the test for an example of the reporting.
