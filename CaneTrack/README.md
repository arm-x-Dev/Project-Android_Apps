CaneTracker 🚜
==============

**CaneTracker** is a streamlined mobile application designed to help farmers and transport managers track sugarcane loading in real-time. It simplifies the process of calculating net harvest weight by managing worker profiles and automating scale subtractions.

📋 Features
-----------

*   **Team Management:** Register worker details including names and their base body weights.
    
*   **Trip Tracking:** Add individual "trips" or loads for each worker during the loading process.
    
*   **Live Weight Calculation:** Enter the gross scale reading; the app automatically handles the math.
    
*   **Loading Logs:** Maintain a digital record of how much sugarcane has been loaded into a specific vehicle.
    

⚙️ Core Logic
-------------

The application follows a simple but effective subtraction logic to ensure accuracy without manual calculation errors.

1\. Worker Setup
----------------

Before loading starts, workers are weighed. This is saved as the BaseWeight.

> **Example:** Worker "Raj" weighs 70kg.

2\. The Loading Process
-----------------------

When a worker carries sugarcane onto the weighing scale:

1.  The user selects the **Worker Name**.
    
2.  The user enters the **Gross Scale Reading** (Worker + Sugarcane).
    
3.  The app performs the following calculation:
    

$$Net\\ Cane\\ Weight = Gross\\ Scale\\ Reading - Worker\\ Base\\ Weight$$

3\. Data Entry Workflow
-----------------------

1.  **Click "Add Trip"** on the specific worker's profile.
    
2.  **Enter Scale Reading** from the physical weighing machine.
    
3.  **Save:** The app logs the net weight and updates the total vehicle load.
    

🚀 Future Enhancements
----------------------

*   **PDF Reports:** Generate daily loading summaries for factory submission.
    
*   **Cloud Sync:** Allow multiple devices to sync to one "Vehicle Trip."
    
*   **Offline Mode:** Ensure functionality in remote fields with no internet access.
