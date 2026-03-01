# Continue Beans - Ollama Docker Setup

## 🐳 Docker Setup for Ollama

This setup provides a complete Docker environment for running Ollama with the qwen2.5:7b model for Continue Beans.

## 🚀 Quick Start

### 1. Install Docker
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install docker.io docker-compose

# Or visit: https://docs.docker.com/get-docker/
```

### 2. Run Setup Script
```bash
cd /home/bajinho/GitHub/continue-netbeans
chmod +x docker/setup-ollama.sh
./docker/setup-ollama.sh
```

### 3. Manual Setup (Alternative)
```bash
cd docker
docker-compose up -d ollama
docker exec -it continue-beans-ollama ollama pull qwen2.5:7b
```

## 📋 Services

### Ollama Service
- **Port**: 11434
- **Volume**: `ollama_data` (persistent model storage)
- **Model**: qwen2.5:7b (7B parameters, optimized for function calling)

### Model Puller Service (Optional)
```bash
# Pull models automatically
docker-compose --profile pull-models up model-puller
```

## 🔧 Configuration

### Environment Variables
- `OLLAMA_HOST=0.0.0.0` - Accept connections from any host

### Volumes
- `ollama_data:/root/.ollama` - Persistent model storage

### Networks
- `continue-beans-network` - Isolated Docker network

## 🎯 Usage

### 1. Start Ollama
```bash
docker-compose up -d ollama
```

### 2. Check Status
```bash
docker-compose ps
curl http://127.0.0.1:11434/api/tags
```

### 3. Test Model
```bash
docker exec -it continue-beans-ollama ollama run qwen2.5:7b "Hello, how are you?"
```

### 4. List Models
```bash
docker exec -it continue-beans-ollama ollama list
```

## 🛠️ Management

### Stop Services
```bash
docker-compose down
```

### Restart Services
```bash
docker-compose restart
```

### View Logs
```bash
docker-compose logs -f ollama
```

### Update Model
```bash
docker exec -it continue-beans-ollama ollama pull qwen2.5:7b
```

## 🔍 Troubleshooting

### Port Already in Use
```bash
# Check what's using port 11434
sudo netstat -tulpn | grep 11434

# Change port in docker-compose.yml
ports:
  - "11435:11434"
```

### Permission Issues
```bash
# Add user to docker group
sudo usermod -aG docker $USER
# Logout and login again
```

### Model Not Found
```bash
# Pull model manually
docker exec -it continue-beans-ollama ollama pull qwen2.5:7b
```

### Memory Issues
```bash
# Check Docker memory limits
docker system df
docker system prune
```

## 🚀 Integration with Continue Beans

### 1. Update ChatPanel
Make sure `OllamaChatPanel.java` is configured for:
- URL: `http://127.0.0.1:11434`
- Model: `qwen2.5:7b`

### 2. Install Plugin
```bash
# Build and install NetBeans plugin
mvn clean install nbm:cluster
# Install target/nbm/continue-beans-1.0-SNAPSHOT.nbm in NetBeans
```

### 3. Test Integration
- Open NetBeans
- Window → Continue Beans Chat (Ollama)
- Test: `crie hello world em python`

## 📊 Model Information

### qwen2.5:7b
- **Parameters**: 7B
- **Size**: ~4.7GB
- **Specialization**: Function calling, code generation
- **Language**: Multilingual (strong in English/Chinese)
- **Performance**: Excellent for development tasks

### Alternative Models
```bash
# Other models you can try:
docker exec -it continue-beans-ollama ollama pull llama3.1:8b
docker exec -it continue-beans-ollama ollama pull mistral-nemo:latest
docker exec -it continue-beans-ollama ollama pull codellama:7b
```

## 🏆 Benefits

✅ **Isolated Environment** - No system dependencies
✅ **Persistent Storage** - Models survive container restarts
✅ **Easy Management** - Simple start/stop commands
✅ **Portability** - Works on any Docker-enabled system
✅ **Resource Control** - Limit memory/CPU usage if needed
✅ **Version Control** - Easy to rollback/upgrade models

## 🔗 Links

- [Ollama Documentation](https://ollama.com/documentation)
- [Docker Documentation](https://docs.docker.com/)
- [Continue Beans Project](https://github.com/bajinho/continue-netbeans)
