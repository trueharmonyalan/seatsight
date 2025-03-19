
import cv2
import threading
import time
import queue
import logging
import numpy as np
from datetime import datetime

logger = logging.getLogger(__name__)

class CaptureThread(threading.Thread):
    """
    Thread to continuously capture frames from a camera or video source.
    """
    
    def __init__(self, source, queue_size=30, name=None, target_resolution=None):
        """
        Initialize the capture thread.
        
        Args:
            source: URL or ID of the camera/video source
            queue_size: Maximum number of frames to store in the queue
            name: Thread name
            target_resolution: Optional tuple (width, height) to resize frames
        """
        super().__init__(name=name)
        self.source = source
        self.frame_queue = queue.Queue(maxsize=queue_size)
        self.running = False
        self.cap = None
        self.daemon = True
        self.logger = logging.getLogger(name if name else __name__)
        self.fps = 0
        self.last_frame_time = None
        self.frame_count = 0
        
        # Set target resolution for resize
        if target_resolution:
            self.target_width, self.target_height = target_resolution
            self.logger.info(f"Will resize frames to: {self.target_width}x{self.target_height}")
        else:
            self.target_width, self.target_height = None, None
        
    def start(self):
        """Start the capture thread."""
        self.running = True
        super().start()
        
    def stop(self):
        """Stop the capture thread."""
        self.running = False
        if self.cap:
            self.cap.release()
        
    def run(self):
        """Main thread function to capture frames."""
        self.logger.info(f"Starting capture from source: {self.source}")
        
        # Try to connect to the camera
        try:
            if isinstance(self.source, str) and 'rtsp://' in self.source:
                self.logger.info(f"Opening RTSP stream: {self.source}")
                # Use UDP transport for RTSP streams
                self.cap = cv2.VideoCapture(self.source, cv2.CAP_FFMPEG)
            else:
                self.logger.info(f"Opening camera/video: {self.source}")
                self.cap = cv2.VideoCapture(self.source)
            
            if not self.cap.isOpened():
                self.logger.error(f"Failed to open video source: {self.source}")
                self.running = False
                return
                
            self.logger.info("Video source opened successfully")
            
            # Get original resolution and FPS
            orig_width = int(self.cap.get(cv2.CAP_PROP_FRAME_WIDTH))
            orig_height = int(self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
            src_fps = self.cap.get(cv2.CAP_PROP_FPS)
            self.logger.info(f"Source resolution: {orig_width}x{orig_height}, FPS: {src_fps}")
            
            # Set buffer size
            self.cap.set(cv2.CAP_PROP_BUFFERSIZE, 3)
            
            # Main capture loop
            frame_count = 0
            last_log_time = time.time()
            start_time = time.time()
            
            while self.running:
                ret, frame = self.cap.read()
                
                if not ret:
                    self.logger.warning("Failed to read frame, reconnecting...")
                    # Try to reconnect
                    self.cap.release()
                    time.sleep(2)
                    self.cap = cv2.VideoCapture(self.source)
                    if not self.cap.isOpened():
                        self.logger.error("Failed to reconnect to video source")
                        time.sleep(5)  # Wait before trying again
                    continue
                
                # Resize frame to lower resolution immediately to save processing power
                if self.target_width and self.target_height:
                    frame = cv2.resize(frame, (self.target_width, self.target_height), 
                                       interpolation=cv2.INTER_AREA)
                
                # Calculate actual FPS
                frame_count += 1
                now = time.time()
                self.last_frame_time = now
                
                # Log FPS every 5 seconds
                if now - last_log_time >= 5.0:
                    elapsed = now - last_log_time
                    self.fps = frame_count / elapsed
                    self.logger.debug(f"Capture FPS: {self.fps:.1f}")
                    last_log_time = now
                    frame_count = 0
                
                # Add timestamp to the frame
                timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                cv2.putText(frame, timestamp, (10, 30), 
                            cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
                
                # Add frame to queue, dropping old frames if full
                try:
                    self.frame_queue.put(frame, block=False)
                except queue.Full:
                    # Queue is full, get a frame to make room
                    try:
                        self.frame_queue.get_nowait()
                        self.frame_queue.put(frame, block=False)
                    except:
                        pass
                
                # Brief sleep to reduce CPU usage
                time.sleep(0.001)
                
        except Exception as e:
            self.logger.error(f"Error in capture thread: {e}")
            
        finally:
            if self.cap:
                self.cap.release()
            self.logger.info("Capture thread stopped")
    
    def get_frame(self, timeout=1.0):
        """
        Get the latest frame from the queue.
        
        Args:
            timeout: How long to wait for a frame in seconds
        
        Returns:
            Latest frame or None if queue is empty or timeout occurs
        """
        try:
            return self.frame_queue.get(timeout=timeout)
        except queue.Empty:
            return None

class VideoProcessor:
    """
    Handles video processing operations including capturing, processing, and displaying.
    """
    
    def __init__(self, source, process_every_n=1, resolution=None):
        """
        Initialize the video processor.
        
        Args:
            source: Camera URL or device ID
            process_every_n: Process every Nth frame to reduce load
            resolution: Target resolution (width, height) or None for source resolution
        """
        self.source = source
        self.process_every_n = process_every_n
        self.resolution = resolution
        self.is_running = False
        self.frame_count = 0
        self.last_processed_frame = None
        self.last_processed_time = 0
        self.fps = 0
        self.logger = logging.getLogger(__name__)
        self.capture_thread = None
        
    def start(self):
        """Start video capture and processing."""
        if self.is_running:
            return
            
        self.is_running = True
        self.logger.info(f"Starting video processor for source: {self.source}")
        
        # Create and start the capture thread with target resolution
        self.capture_thread = CaptureThread(
            source=self.source,
            queue_size=30,
            name=f"CaptureThread_{self.source}",
            target_resolution=self.resolution  # Pass resolution here
        )
        self.capture_thread.start()
        
    def stop(self):
        """Stop video capture and processing."""
        if not self.is_running:
            return
            
        self.is_running = False
        self.logger.info("Stopping video processor")
        
        if self.capture_thread:
            self.capture_thread.stop()
            self.capture_thread.join(timeout=2.0)
            
    def get_frame(self):
        """
        Get the latest frame from the capture thread.
        
        Returns:
            Latest frame or None
        """
        if not self.capture_thread:
            return None
            
        return self.capture_thread.get_frame()
    
    def should_process_frame(self):
        """
        Determine if the current frame should be processed based on the skip count.
        
        Returns:
            bool: True if the current frame should be processed
        """
        self.frame_count += 1
        return self.frame_count % self.process_every_n == 0