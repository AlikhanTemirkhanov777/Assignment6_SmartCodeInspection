# Assignment 6 — краткие ответы для защиты

**Что делает программа?** Анализирует проект fintech-project: безопасность, статистику, готовность к релизу, соответствие правилам и технический долг.

**Почему Visitor?** Есть пять типов элементов, но несколько независимых проверок. Вместо `checkSecurity()` и `checkCompliance()` в каждом классе я вынес проверки в отдельных Visitors.

**Где Element?** `model/ProjectElement.java` и классы `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile`, `Directory`.

**Где Visitor?** `visitor/ProjectVisitor.java` с пятью перегруженными `visit()` и пять реализаций.

**Double dispatch:** `projectElement.accept(visitor)` вызывает `visitor.visit(this)`. Java выбирает обработчик по конкретному типу элемента и конкретному объекту visitor.

**Почему Directory?** Позволяет делать дерево проекта. Каждый Visitor рекурсивно обходит детей: `child.accept(this)`.

**Дополнительный visitor?** `TechnicalDebtVisitor`: считает баллы за сложный код, уязвимые зависимости, ошибки тестов, плохую конфигурацию. Его добавили отдельным коммитом, не меняя Elements.

**Что такое Quality Gate?** Проверяет безопасность, среднюю сложность <= 10 и отношение тестовых файлов к исходным >= 0.40. В нашей демонстрации `1/2=0.50`, поэтому Tests PASS.

**Почему Compliance FAIL, хотя Quality Gate PASS?** Quality Gate проверяет количественные пороги, а Compliance требует покрыть каждый production-файл тестом. Для `src/UserService.java` теста нет.

**Что если новый Visitor?** Написать класс `implements ProjectVisitor`, реализовать 5 методов `visit()`. Elements не менять.

**Что если новый DatabaseMigrationFile?** Добавить новый Element, новый `visit(DatabaseMigrationFile)` в интерфейс и реализовать его в каждом Visitor; иначе ошибки компиляции.

**Visitor vs Strategy:** Strategy меняет один алгоритм в контексте; Visitor делает разные операции над разными типами элементов через `accept/visit`.

**SOLID:** SRP — модель отдельно, анализ отдельно. OCP — можно добавлять новых Visitors. DIP — используется интерфейс `ProjectVisitor`.

**Live coding порядок:** Predict -> Explain -> Modify -> Run -> Test. Сначала скажи, какие классы меняются, потом делай изменения, затем запусти JUnit и Demo.

**Команды (если Maven в PATH):** `mvn clean test` и `mvn -q exec:java -Dexec.mainClass=kz.edu.assignment6.demo.DemoApplication`.
