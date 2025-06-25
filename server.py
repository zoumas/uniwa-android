#!/usr/bin/env python3
# server.py

import socket
import socketserver
import threading
import platform
import random
import time

PORT = 41007


class LabRequestHandler(socketserver.StreamRequestHandler):
    def handle(self):
        hostname = socket.gethostname()
        osname = platform.system()
        # Μετατρέπουμε π.χ. 'Linux' σε 'Linux', ή ό,τι επιστρέφει η platform.system()
        while True:
            data = self.rfile.readline()
            if not data:
                break

            cmd = data.strip().decode("utf-8")
            print(f"[SERVER] host:{host} os:{osname} cmd:{cmd}")

            if cmd == "Echo":
                response = f"{hostname} - {osname}\n"
            elif cmd == "Restart":
                response = f"{hostname} Rebooting...\n"
            elif cmd == "Shutdown":
                response = f"{hostname} Shutting down...\n"
            elif cmd == "Restore":
                response = f"{hostname} - Restoring\n"
                # τυχαίο διάστημα 60–120 δευτ.
                delay = random.randint(60, 120)
                time.sleep(delay)
                response += f"{hostname} - Restored\n"
            else:
                response = f"Unknown command: {cmd}\n"
            try:
                self.wfile.write(response.encode("utf-8"))
            except BrokenPipeError:
                break


class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    # Επανεκκινούμε χωρίς να περιμένουμε TIME_WAIT
    allow_reuse_address = True


if __name__ == "__main__":
    host = ""  # ακούει σε όλες τις διεπαφές
    with ThreadedTCPServer((host, PORT), LabRequestHandler) as server:
        print(f"Starting Python LabControl server on port {PORT}")
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            print("\nShutting down server.")
            server.shutdown()
