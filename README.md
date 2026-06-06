# WEWEWWATCH

Android-приложение на Kotlin/Jetpack Compose для лабораторной работы WeWatch.

## Что реализовано

- MainScreen со списком выбранных фильмов, пустым состоянием и удалением отмеченных фильмов.
- AddScreen с вводом названия и года, отображением выбранного фильма и добавлением в локальную SQLite БД.
- SearchScreen с поиском фильмов через The Open Movie Database и выбором результата.
- Хранение выбранных фильмов в Room БД.
- Архитектура MVI: Compose-экраны отправляют intents, читают immutable state и реагируют на effects.
- Чистая архитектура: presentation работает через use case, а data-слой реализует доменный контракт.
- Загрузка постеров по URL без дополнительных библиотек.

## Архитектура

### Data

- `data/AppDatabase.kt`, `MovieDao.kt`, `MovieModels.kt` - локальная Room БД.
- `data/OmdbClient.kt` - удаленный источник данных OMDb.
- `data/MovieRepository.kt` - реализация доменного репозитория для локальных и удаленных данных.

### Domain

- `domain/repository/MovieRepositoryContract.kt` - контракт репозитория для бизнес-логики.
- `domain/usecase/ObserveWatchListUseCase.kt` - получение списка фильмов.
- `domain/usecase/AddMovieToWatchListUseCase.kt` - добавление фильма.
- `domain/usecase/DeleteMoviesUseCase.kt` - удаление выбранных фильмов.
- `domain/usecase/SearchMoviesUseCase.kt` - поиск фильмов.

### Presentation

- `ui/mvi/MovieListContract.kt` - `MovieListState`, `MovieListIntent` и `MovieListEffect`.
- `ui/mvi/SearchContract.kt` - `SearchState` и `SearchIntent`.
- `ui/viewmodel/MovieListViewModel.kt` - MVI reducer главного экрана, работающий через use case.
- `ui/viewmodel/SearchViewModel.kt` - MVI reducer поиска фильмов, работающий через use case.
- `MainActivity.kt` - навигация и Compose UI.

## Чистая архитектура

Presentation-слой не обращается к `MovieRepository` напрямую. ViewModel получает готовые use case, отправляет в них пользовательские сценарии и обновляет MVI state. Data-слой скрыт за `MovieRepositoryContract`, поэтому бизнес-логика зависит от абстракции.

## MVI поток

1. Пользователь выполняет действие на экране.
2. UI отправляет intent во ViewModel.
3. ViewModel обновляет state через reducer.
4. UI перерисовывается из нового state.
5. Одноразовые события, например возврат на главный экран после добавления фильма, приходят как effect.

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
