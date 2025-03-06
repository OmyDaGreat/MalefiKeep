# Kobweb Notes

A Google Keep-style notes application built with Kobweb (Kotlin for Web).

## Features
- Create and delete notes
- Change note colors
- Simple and responsive interface

## Development

### Prerequisites
- JDK 17 or newer
- Gradle

### Running locally

1. Clone this repository
```
git clone https://github.com/OmyDaGreat/kobweb-notes.git
cd kobweb-notes
```

1. Start the development server
```
./gradlew kobwebStart
```

1. Open your browser and navigate to `http://localhost:8080`

### Building for production

```
./gradlew kobwebExport
```

The production files will be generated in the `.kobweb/site` directory.

## Docker Support

### Building and running with Docker

```
docker build -t kobweb-notes .
docker run -p 8080:80 kobweb-notes
```

### Running with Docker Compose

```
docker-compose up
```

Access the application at `http://localhost:8080`

## Technology Stack
- Kotlin
- Kobweb (Kotlin Compose for Web)
- Docker
