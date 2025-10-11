# Data Export UI - Quick Start Guide

## Overview
The Data Export UI provides administrators with a comprehensive interface to export system data in various formats (PDF, CSV) for reporting and analysis purposes.

## Accessing the Export UI

### Navigation
1. Log in as an admin user
2. Navigate to the Admin Dashboard
3. Select the "Export" tab from the bottom navigation (once navigation is updated in Task 15)

**Alternative:** For now, the fragment can be accessed programmatically or added to the navigation manually.

---

## Using the Export Feature

### Step 1: Configure Export Settings

#### Select Export Format
Choose between two format options:
- **PDF Document** - Formatted report with charts and tables
- **CSV Spreadsheet** - Raw data in comma-separated format

#### Select Data Type
Choose what data to export:
- **Items Report** - All lost and found items
- **Users Report** - User accounts and statistics
- **Activity Logs** - System activity history
- **Comprehensive Report** - All data combined

#### Select Date Range
1. Click "Select Date Range" button
2. Choose start and end dates from the Material DatePicker
3. The selected range will be displayed below the button
4. Default: Last 30 days

### Step 2: Generate Export

1. Click the "Generate Export" button
2. A progress dialog will appear showing:
   - Progress percentage (0-100%)
   - Current status message
   - Cancel button (if needed)

**Progress Stages:**
- 0-20%: Preparing export...
- 20-50%: Collecting data...
- 50-80%: Generating file...
- 80-100%: Finalizing...

### Step 3: Access Exported File

Once the export completes:
1. A success message will appear
2. The export will be added to the Export History
3. The file is saved to: `Documents/LostFoundExports/`

---

## Managing Export History

### Viewing Export History

The Export History section displays all previous exports with:
- **Title:** Data type (e.g., "Comprehensive Report")
- **Format:** PDF or CSV badge
- **Status:** Color-coded status indicator
  - 🟢 Green = Completed
  - 🔴 Red = Failed
  - 🟠 Orange = Processing
  - ⚫ Gray = Pending
- **Date:** When the export was created
- **File Size:** Size of the exported file (if available)

### Export Actions

Click on any export in the history to see available actions:

#### 1. View File
- Opens the export file in an appropriate app
- PDF files open in PDF readers
- CSV files open in spreadsheet apps
- Requires compatible app installed

#### 2. Share File
- Opens Android share sheet
- Share via email, messaging, cloud storage, etc.
- File is shared securely using FileProvider

#### 3. Re-download
- Regenerates the export with the same parameters
- Creates a new export with current data
- Useful for getting updated data

#### 4. Delete
- Removes the export file from storage
- Deletes the export from history
- Requires confirmation

### Cleanup Old Exports

To free up storage space:
1. Click the "Cleanup" button in the Export History header
2. Confirm the cleanup action
3. All exports older than 30 days will be deleted
4. A success message shows how many exports were removed

---

## Export File Locations

### Storage Path
Exports are saved to:
```
/storage/emulated/0/Android/data/com.example.loginandregistration/files/Documents/LostFoundExports/
```

### File Naming Convention
Files are named using the pattern:
```
{data_type}_{timestamp}.{extension}

Examples:
- comprehensive_report_1705334400000.pdf
- items_report_1705334400000.csv
- users_report_1705334400000.pdf
```

---

## Export Formats

### PDF Format
**Best for:**
- Formal reports
- Presentations
- Printing
- Sharing with non-technical users

**Contains:**
- Title and date range
- Summary statistics
- Formatted tables
- Charts and graphs (for comprehensive reports)
- Page numbers and headers

### CSV Format
**Best for:**
- Data analysis
- Importing into other systems
- Spreadsheet manipulation
- Database imports

**Contains:**
- Header row with column names
- Raw data in comma-separated format
- Proper escaping for special characters
- UTF-8 encoding

---

## Data Types Explained

### Items Report
**Includes:**
- Item ID, name, description
- Category and location
- Status (Lost, Found, Returned, etc.)
- Reporter information
- Timestamps (reported, updated)
- Request and return information
- Donation status

**Use Cases:**
- Inventory management
- Item tracking
- Donation planning
- Statistical analysis

### Users Report
**Includes:**
- User ID, email, display name
- Role (User, Admin, Security)
- Account status (Active, Blocked)
- Registration date
- Last login timestamp
- Activity statistics (items reported, found, claimed)
- Block information (if applicable)

**Use Cases:**
- User management
- Access control auditing
- User engagement analysis
- Account verification

### Activity Logs
**Includes:**
- Log ID and timestamp
- Actor (user who performed action)
- Action type (login, item report, status change, etc.)
- Target entity (user, item, etc.)
- Previous and new values
- Device information
- IP address (if available)

**Use Cases:**
- Security auditing
- Compliance reporting
- Troubleshooting
- User behavior analysis

### Comprehensive Report
**Includes:**
- All of the above data types
- Executive summary
- Cross-referenced statistics
- Trend analysis
- System health metrics

**Use Cases:**
- Management reporting
- Annual reviews
- System audits
- Stakeholder presentations

---

## Troubleshooting

### Export Fails to Generate

**Possible Causes:**
1. **No data in date range** - Select a wider date range
2. **Storage permission denied** - Grant storage permissions in app settings
3. **Insufficient storage space** - Free up device storage
4. **Network error** - Check internet connection (for Firestore access)

**Solutions:**
- Check error message for specific issue
- Verify date range contains data
- Ensure storage permissions are granted
- Try a smaller date range
- Restart the app

### Cannot Open Export File

**Possible Causes:**
1. **No compatible app installed** - Install PDF reader or spreadsheet app
2. **File not found** - Export may have been deleted
3. **Corrupted file** - Re-generate the export

**Solutions:**
- Install Adobe Acrobat Reader (for PDF)
- Install Google Sheets or Excel (for CSV)
- Use "Re-download" option to regenerate
- Check if file exists in storage

### Share Function Not Working

**Possible Causes:**
1. **No sharing apps available** - Install email or messaging app
2. **File permissions issue** - FileProvider configuration error
3. **File too large** - Some apps have size limits

**Solutions:**
- Install email app (Gmail, Outlook)
- Try sharing via different app
- For large files, use cloud storage
- Check app permissions

### Export History Not Loading

**Possible Causes:**
1. **Network error** - Cannot connect to Firestore
2. **No exports created yet** - Create first export
3. **Permission error** - Admin access issue

**Solutions:**
- Check internet connection
- Verify admin credentials
- Refresh the fragment
- Check Firestore security rules

---

## Best Practices

### Export Frequency
- **Daily:** Activity logs for security monitoring
- **Weekly:** Items and users reports for management
- **Monthly:** Comprehensive reports for stakeholders
- **As Needed:** Specific data for investigations

### Storage Management
- Run cleanup monthly to free space
- Delete unnecessary exports after use
- Keep important exports backed up to cloud
- Monitor storage usage in device settings

### Data Security
- Don't share exports containing sensitive data
- Delete exports after viewing if not needed
- Use secure channels for sharing (encrypted email)
- Be aware of data privacy regulations

### Performance Tips
- Use smaller date ranges for faster exports
- Export during off-peak hours for large datasets
- Close other apps to free memory
- Ensure stable internet connection

---

## Keyboard Shortcuts & Gestures

### Fragment Navigation
- **Swipe left/right:** Navigate between tabs (if implemented)
- **Pull down:** Refresh export history

### Dialog Interactions
- **Back button:** Cancel export (if dialog is dismissible)
- **Tap outside:** Dismiss completed export dialog

---

## Permissions Required

### Storage Permissions
- **READ_EXTERNAL_STORAGE:** Read existing exports
- **WRITE_EXTERNAL_STORAGE:** Save new exports (Android 10 and below)

### Network Permissions
- **INTERNET:** Access Firestore for data and history

### Notification Permissions (Optional)
- **POST_NOTIFICATIONS:** Show export completion notifications (Android 13+)

---

## Support & Feedback

### Common Questions

**Q: How long does an export take?**
A: Typically 5-30 seconds depending on data size and device performance.

**Q: What's the maximum file size?**
A: No hard limit, but large exports (>50MB) may be slow to generate and share.

**Q: Can I schedule automatic exports?**
A: Not currently, but this is a planned feature enhancement.

**Q: Are exports encrypted?**
A: Files are stored unencrypted. Use device encryption for security.

**Q: Can I export to cloud storage directly?**
A: Not directly, but you can share to cloud storage apps after export.

### Getting Help

If you encounter issues:
1. Check this guide for troubleshooting steps
2. Review error messages carefully
3. Check app permissions in device settings
4. Contact system administrator
5. Report bugs to development team

---

## Version Information

**Feature Version:** 1.0  
**Last Updated:** January 2024  
**Compatible With:** Android 8.0 (API 26) and above  
**Dependencies:** Firebase Firestore, Material Design 3

---

## Related Documentation

- [Data Export Implementation Summary](DATA_EXPORT_IMPLEMENTATION_SUMMARY.md)
- [Admin Dashboard User Guide](readme files/ADMIN_DASHBOARD_README.md)
- [Firebase Setup Guide](readme files/FIREBASE_SETUP_GUIDE.md)
- [Complete Setup Checklist](readme files/COMPLETE_SETUP_CHECKLIST.md)
