# WEWEWWATCH

Android-приложение на Kotlin/Jetpack Compose для лабораторной работы WeWatch.

## Что реализовано

- MainScreen со списком выбранных фильмов, пустым состоянием и удалением отмеченных фильмов.
- AddScreen с вводом названия и года, отображением выбранного фильма и добавлением в локальную SQLite БД.
- SearchScreen с поиском фильмов через The Open Movie Database и выбором результата.
- Хранение выбранных фильмов в Room БД.
- Архитектура MVVM: Compose-экраны читают состояние из ViewModel.
- Общий MovieRepository для доступа к OMDb и локальной базе.
- Загрузка постеров по URL без дополнительных библиотек.

## Архитектура

- `data/AppDatabase.kt`, `MovieDao.kt`, `MovieModels.kt` - локальная Room БД.
- `data/OmdbClient.kt` - удаленный источник данных OMDb.
- `data/MovieRepository.kt` - общий репозиторий для локальных и удаленных данных.
- `ui/viewmodel/MovieListViewModel.kt` - состояние главного экрана и операции со списком.
- `ui/viewmodel/SearchViewModel.kt` - состояние поиска фильмов.
- `MainActivity.kt` - навигация и Compose UI.

## OMDb API key

Получите ключ на сайте OMDb и добавьте его в `local.properties`:

```properties
OMDB_API_KEY=your_key_here
```

Файл `local.properties` не коммитится, поэтому ключ не попадет в GitHub.

## Сборка

Откройте проект в Android Studio или выполните:

```powershell
.\gradlew.bat :app:assembleDebug
```
