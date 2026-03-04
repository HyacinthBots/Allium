# escape=\
# syntax=docker/dockerfile:1

FROM azul/zulu-openjdk-alpine:21-latest

# Create required directories
RUN mkdir -p /bot/plugins
RUN mkdir -p /bot/data
RUN mkdir -p /dist/out

# Declare required volumes
VOLUME [ "/bot/data" ]
VOLUME [ "/bot/plugins" ]

# Copy the distribution files into the container
COPY [ "build/distributions/allium-eww-testing-on-windows.tar", "/dist" ]

# Extract the distribution files, and prepare them for use
RUN tar -xf /dist/allium-eww-testing-on-windows.tar -C /dist/out
RUN chmod +x /dist/out/allium-eww-testing-on-windows/bin/allium

# Clean up unnecessary files
RUN rm /dist/allium-eww-testing-on-windows.tar

# Set the correct working directory
WORKDIR /bot

# Run the distribution start script
ENTRYPOINT [ "/dist/out/allium-eww-testing-on-windows/bin/allium" ]
