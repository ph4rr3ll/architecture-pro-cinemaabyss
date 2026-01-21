package main

import (
	"log"
	"math/rand"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"strconv"
	"strings"
)

type ProxyConfig struct {
	Port                   string
	MonolithURL            string
	MoviesServiceURL       string
	EventsServiceURL       string
	GradualMigration       bool
	MoviesMigrationPercent int
}

var config ProxyConfig

func main() {
	// Load configuration from environment variables
	loadConfig()

	// Set up HTTP routes
	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/api/movies", moviesProxyHandler)
	http.HandleFunc("/", defaultProxyHandler)

	// Start server
	log.Printf("Starting proxy service on port %s", config.Port)
	log.Printf("Gradual migration enabled: %v", config.GradualMigration)
	log.Printf("Movies migration percent: %d%%", config.MoviesMigrationPercent)
	log.Fatal(http.ListenAndServe(":"+config.Port, nil))
}

func loadConfig() {
	config.Port = getEnv("PORT", "8000")
	config.MonolithURL = getEnv("MONOLITH_URL", "http://localhost:8080")
	config.MoviesServiceURL = getEnv("MOVIES_SERVICE_URL", "http://localhost:8081")
	config.EventsServiceURL = getEnv("EVENTS_SERVICE_URL", "http://localhost:8082")

	config.GradualMigration = strings.ToLower(getEnv("GRADUAL_MIGRATION", "false")) == "true"

	migrationPercent, err := strconv.Atoi(getEnv("MOVIES_MIGRATION_PERCENT", "0"))
	if err != nil {
		log.Printf("Warning: Invalid MOVIES_MIGRATION_PERCENT, defaulting to 0")
		migrationPercent = 0
	}
	config.MoviesMigrationPercent = migrationPercent
}

func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(`{"status": "ok"}`))
}

// moviesProxyHandler handles requests to /api/movies with gradual migration logic
func moviesProxyHandler(w http.ResponseWriter, r *http.Request) {
	var targetURL string

	if config.GradualMigration && config.MoviesMigrationPercent > 0 {
		// Generate a random number between 0 and 99
		randomValue := rand.Intn(100)

		// If random value is less than migration percent, route to movies microservice
		if randomValue < config.MoviesMigrationPercent {
			targetURL = config.MoviesServiceURL
			log.Printf("Routing /api/movies request to movies microservice (random: %d, threshold: %d)",
				randomValue, config.MoviesMigrationPercent)
		} else {
			targetURL = config.MonolithURL
			log.Printf("Routing /api/movies request to monolith (random: %d, threshold: %d)",
				randomValue, config.MoviesMigrationPercent)
		}
	} else {
		// If gradual migration is disabled or percentage is 0, route to monolith
		targetURL = config.MonolithURL
		log.Printf("Routing /api/movies request to monolith (migration disabled or 0%%)")
	}

	// Parse target URL
	target, err := url.Parse(targetURL)
	if err != nil {
		log.Printf("Error parsing target URL: %v", err)
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}

	// Create reverse proxy
	proxy := httputil.NewSingleHostReverseProxy(target)

	// Customize the request before forwarding
	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
		req.Host = target.Host
		req.URL.Scheme = target.Scheme
		req.URL.Host = target.Host
		req.URL.Path = r.URL.Path
		req.URL.RawQuery = r.URL.RawQuery
	}

	// Handle errors
	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		log.Printf("Proxy error: %v", err)
		http.Error(w, "Bad Gateway", http.StatusBadGateway)
	}

	// Forward the request
	proxy.ServeHTTP(w, r)
}

// defaultProxyHandler handles all other requests (proxy to monolith)
func defaultProxyHandler(w http.ResponseWriter, r *http.Request) {
	// Check if the request is for /api/movies (should not reach here)
	if strings.HasPrefix(r.URL.Path, "/api/movies") {
		moviesProxyHandler(w, r)
		return
	}

	// For all other requests, proxy to monolith
	targetURL := config.MonolithURL
	log.Printf("Routing %s request to monolith", r.URL.Path)

	// Parse target URL
	target, err := url.Parse(targetURL)
	if err != nil {
		log.Printf("Error parsing target URL: %v", err)
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}

	// Create reverse proxy
	proxy := httputil.NewSingleHostReverseProxy(target)

	// Customize the request before forwarding
	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
		req.Host = target.Host
		req.URL.Scheme = target.Scheme
		req.URL.Host = target.Host
		req.URL.Path = r.URL.Path
		req.URL.RawQuery = r.URL.RawQuery
	}

	// Handle errors
	proxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
		log.Printf("Proxy error: %v", err)
		http.Error(w, "Bad Gateway", http.StatusBadGateway)
	}

	// Forward the request
	proxy.ServeHTTP(w, r)
}
