# How to use

Compile with:

```
./gradlew build
```

Run with:

```
java -jar ./build/libs/MCS-1.0-SNAPSHOT.jar <DatapackPath>
```

## Args

Possible arguments:

* `-o`, `--output`: Specifies where the finished `data` folder will be stored.
* `--obfuscate`: Names every function `function-{id}`.
* `--ast`: Prints a debug view of the AST to the console.
* `-v`, `--verbose`: Enables verbose output.
* `-cp`, `--classpath`: Specifies a classpath. This lets the compiler know that code from a library will exist at runtime without compiling the library into the result.

# Syntax

A package path looks like this:

```
this.is.a.path.with.a.Class
```

It does not need to be lowercase, because it gets converted to `snake_case`. A `.` is used instead of `/` so the compiler can parse it more easily.

`print(string s)` is in `std:std.Std`, which is statically imported by default.

Example:

```java
package <namespace>:<packagePath>;

import <namespace>:<packagePath>;

import static <namespace>:<packagePath>;

class <Name> {
    class SomeInnerClass {
        string someField;

        constructor() {
            // Some constructor code...
        }
    }

    function foo() {
        print("Hello World");

        SomeInnerClass innerClassVariable = new SomeInnerClass();
        print($"Hello ${innerClassVariable.someField}");
    }
}
```
