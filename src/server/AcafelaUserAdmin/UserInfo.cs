using System;
using System.ComponentModel;

namespace AcafelaUserAdmin
{
    public class UserInfo : INotifyPropertyChanged
    {
        private bool mSelected;
        private String mEmail;
        private String mPhone;
        private bool mEnabled;

        public bool Selected
        {
            get { return mSelected; }
            set { mSelected = value; OnPropertyChanged("Selected"); }
        }

        public String Email
        {
            get { return mEmail; }
            set { mEmail = value; OnPropertyChanged("Email"); }
        }

        public String Phone
        {
            get { return mPhone; }
            set { mPhone = value; OnPropertyChanged("Phone"); }
        }

        public bool Enabled
        {
            get { return mEnabled; }
            set { mEnabled = value; OnPropertyChanged("Enabled"); }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged(string propertyName)
        {
            if (string.IsNullOrEmpty(propertyName) || PropertyChanged == null)
                return;
            PropertyChanged?.Invoke(
                                this,
                                new PropertyChangedEventArgs(propertyName));
        }
    }
}
