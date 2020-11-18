# Java-CodeScreen-Excessive-Trade-Cancelling
Java excessive trade cancelling test.

`src/main/resources/Trades.data` is a large CSV file that contains a list of trade messages, one per line in the
 following format:

`Time of trade message, CompanyName, Order Type - New order (D) or Cancel (F), Quantity`

The lines are time ordered although two or more lines may have the same time.

Here are some example lines:

`2015-02-28 07:58:14,Bank of Mars,D,140`

`2015-02-28 08:00:14,Bank of Mars,D,200`

`2015-02-28 08:01:13,Bank of Mars,F,200`

`2015-02-28 08:04:29,Joe traders,D,110`

`2015-02-28 08:05:22,Joe traders,F,11`

`2015-02-28 08:05:25,Joe traders,D,70`

Company names will not contain any commas. Ignore any lines which are not properly formatted and continue to process
the rest of the file.

If, in any given 60 second period and for a given company, the ratio of the cumulative quantity of cancels to cumulative
 quantity of orders is greater than 1:3 then the company is engaged in excessive cancelling.

Consider the above lines. During the period 08:00:14 to 08:01:13 `Bank of Mars` made 400 new orders and cancels,
of which 200 were cancels. This is 50% and is excessive cancelling. `Joe traders` did not engage in excessive cancelling.

During the period 08:00:14 to 08:01:13 the Company `Bank of Mars` engaged in excessive cancelling. In this period 50%
of trades `Bank of Mars` submitted, by quantity, were cancels.

## Your Task

You are required to parse the [Trades.data](src/main/resources/Trades.data) file and implement **ALL** the methods in the
[ExcessiveTradeCancellingChecker](src/main/java/dev/codescreen/cancelling/ExcessiveTradeCancellingChecker.java) class.

The unit tests in the [ExcessiveTradeCancellingCheckerTest](src/test/java/dev/codescreen/cancelling/ExcessiveTradeCancellingCheckerTest.java) class should pass if the methods
in [ExcessiveTradeCancellingChecker](src/main/java/dev/codescreen/cancelling/ExcessiveTradeCancellingChecker.java) are implemented correctly.

## Requirements

The [Trades.data](src/main/resources/Trades.data) file and the [ExcessiveTradeCancellingCheckerTest](src/test/java/dev/codescreen/cancelling/ExcessiveTradeCancellingCheckerTest.java) class should not be modified. If you would like
to add your own unit tests, you can add these in a separate class.

The [pom.xml](pom.xml) file should only be modified in order to add any third-party dependencies required for your solution.

Your solution must use `Java 8`.

##

Good luck!

## Submitting your solution

Please push your changes to the `master branch` of this repository. You can push one or more commits. <br>

Once you are finished with the task, please click the `Complete task` link on <a href="https://app.codescreen.dev/#/codescreentestd390eb38-7c68-43c6-82cf-35d31ebad966" target="_blank">this screen</a>.