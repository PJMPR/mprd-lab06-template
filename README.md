# 🏦 Bank App — model domenowy bankowości internetowej

Repozytorium przedstawia rozwijany krok po kroku model domenowy fikcyjnego banku internetowego. Celem projektu jest poznanie Domain-Driven Design (DDD) przez wydzielanie subdomen, ustalanie wspólnego języka oraz implementowanie reguł biznesowych w osobnych modułach.

Główną domeną jest **bankowość internetowa**. Obejmuje ona dostęp klienta do aplikacji, prowadzenie rachunków, obsługę przelewów, zarządzanie odbiorcami oraz wysyłanie powiadomień. Każda subdomena ma własną odpowiedzialność i będzie rozwijana podczas osobnych zajęć.

> 🎓 **Aktualne laboratorium — agregaty i granice spójności**  
> Studenci uszczelniają istniejący agregat rachunku oraz tworzą Aggregate Root dyspozycji przelewu. Szczegóły znajdują się w [`Zadanie.md`](Zadanie.md).

**Oznaczenia:** ✅ moduł utworzony · 🛠️ aktualnie rozwijany · 📅 temat kolejnych zajęć

---

## 🧩 Subdomeny

| Subdomena | Odpowiedzialność | Moduł | Status |
|---|---|---|---|
| 🔁 Przelewy | Przyjmowanie dyspozycji przelewów i egzekwowanie reguł ich obsługi | [`bank-app-transfer-logic`](bank-app/bank-app-transfer-logic) | ✅ Utworzona · 🛠️ Aggregate Root |
| 💳 Rachunki i środki | Prowadzenie rachunków, salda, dostępnych środków, rezerwacji i historii operacji | [`bank-app-accounts-logic`](bank-app/bank-app-accounts-logic) | ✅ Utworzona · 🛠️ granica agregatu |
| 🔐 Dostęp do bankowości | Logowanie, uwierzytelnianie i dostęp klienta do aplikacji | — | 📅 Inne zajęcia |
| 👥 Odbiorcy | Książka odbiorców i oznaczanie odbiorców zaufanych | — | 📅 Inne zajęcia |
| 🔔 Powiadomienia | Dostarczanie klientowi informacji o zdarzeniach w banku | — | 📅 Inne zajęcia |

---

## 🎯 Aktualny zakres — agregaty i granice spójności

Obie zaimplementowane subdomeny są punktem wyjścia do laboratorium o agregatach. Studenci uszczelniają `Account` jako istniejący Aggregate Root oraz budują Aggregate Root `TransferOrder`, który reprezentuje tożsamość i cykl życia dyspozycji przelewu.

Laboratorium towarzyszy wykładowi [DDD: agregaty i granice spójności](https://pjmpr.github.io/MPR-lectures/lectures/06-ddd-aggregates.html). Pełna treść ćwiczenia, kontrakt modelu i kolejność odblokowywania testów znajdują się w [`Zadanie.md`](Zadanie.md).

Materiały dotyczące wcześniejszej implementacji przelewów pozostają w [`bank-app-transfer-logic/README.md`](bank-app/bank-app-transfer-logic/README.md).

Pozostałe subdomeny wyznaczają dalszy kierunek rozwoju aplikacji. Na tym etapie nie mają jeszcze osobnych modułów ani implementacji.

> [!NOTE]
> Projekt jest edukacyjny. Modele są celowo niewielkie i skupiają się na regułach domenowych, a nie na Springu, bazie danych czy interfejsie REST.

---

## 🗂️ Struktura projektu

Projekt `bank-app` jest wielomodułowym projektem Maven:

```text
bank-app/
├── bank-app-transfer-logic/   # subdomena Przelewy
├── bank-app-accounts-logic/   # subdomena Rachunki i środki
└── pom.xml                    # konfiguracja modułów
```

---

## 🧪 Uruchamianie testów

Wszystkie moduły można zbudować i przetestować z katalogu `bank-app`:

```shell
cd bank-app
mvn test
```

Testy poszczególnych modułów można uruchomić osobno:

```shell
mvn -pl bank-app-accounts-logic test
mvn -pl bank-app-transfer-logic test
```

Nowe testy agregatów są początkowo oznaczone `@Disabled`. Projekt startowy pozostaje zielony; studenci odblokowują kolejne reguły podczas realizacji zadania.

> [!TIP]
> Odblokuj jeden test, doprowadź go do zielonego stanu i dopiero wtedy przejdź do następnego etapu.
